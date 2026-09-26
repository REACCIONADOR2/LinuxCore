package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.BootMode
import com.example.data.model.DistroCatalog
import com.example.data.model.HypervisorHostStats
import com.example.data.model.IsoImage
import com.example.data.model.VirtualFile
import com.example.data.model.VmInstance
import com.example.data.model.VmSnapshot
import com.example.data.model.VmStatus
import com.example.data.repository.VmRepository
import com.example.engine.KernelEmulationEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen {
    DASHBOARD,
    VM_DISPLAY,
    VM_TERMINAL,
    ISO_LIBRARY,
    STORAGE_MANAGER,
    HYPERVISOR_SETTINGS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = VmRepository(database)

    val engine = KernelEmulationEngine(viewModelScope)

    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedVmId = MutableStateFlow<String?>(null)
    val selectedVmId: StateFlow<String?> = _selectedVmId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDistroFilter = MutableStateFlow("ALL")
    val selectedDistroFilter: StateFlow<String> = _selectedDistroFilter.asStateFlow()

    private val _isGridView = MutableStateFlow(true)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _hostStats = MutableStateFlow(HypervisorHostStats())
    val hostStats: StateFlow<HypervisorHostStats> = _hostStats.asStateFlow()

    // Active file being edited in the GUI / storage manager
    private val _activeEditingFile = MutableStateFlow<VirtualFile?>(null)
    val activeEditingFile: StateFlow<VirtualFile?> = _activeEditingFile.asStateFlow()

    private val _currentStoragePath = MutableStateFlow("/home/user")
    val currentStoragePath: StateFlow<String> = _currentStoragePath.asStateFlow()

    // Reactive list of VMs from Room
    val allVms: StateFlow<List<VmInstance>> = repository.allVms
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered VMs for the dashboard
    val filteredVms: StateFlow<List<VmInstance>> = combine(
        allVms,
        searchQuery,
        selectedDistroFilter
    ) { vms, query, filter ->
        vms.filter { vm ->
            val matchesQuery = query.isBlank() ||
                    vm.name.contains(query, ignoreCase = true) ||
                    vm.distroName.contains(query, ignoreCase = true) ||
                    vm.architecture.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                "ALL" -> true
                "RUNNING" -> vm.status == VmStatus.RUNNING || vm.status == VmStatus.BOOTING
                "DEBIAN" -> vm.distroId.contains("ubuntu") || vm.distroId.contains("debian") || vm.distroId.contains("kali") || vm.distroId.contains("mint")
                "ARCH" -> vm.distroId.contains("arch")
                "ALPINE" -> vm.distroId.contains("alpine")
                "REDHAT" -> vm.distroId.contains("fedora")
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Reactive list of ISO images
    val allIsos: StateFlow<List<IsoImage>> = repository.allIsos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentVm: StateFlow<VmInstance?> = combine(allVms, selectedVmId) { vms, id ->
        vms.find { it.id == id } ?: vms.firstOrNull()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    init {
        // Observe VMs to update hypervisor summary stats
        viewModelScope.launch {
            allVms.collect { vms ->
                val active = vms.count { it.status == VmStatus.RUNNING || it.status == VmStatus.BOOTING }
                val allocatedRam = vms.filter { it.status == VmStatus.RUNNING }.sumOf { it.ramMb }
                val totalDisk = vms.sumOf { it.diskSizeGb }
                _hostStats.value = _hostStats.value.copy(
                    activeVmsCount = active,
                    usedRamMb = 3200 + allocatedRam,
                    usedStorageGb = 20 + totalDisk / 2
                )
            }
        }
    }

    fun navigateTo(screen: AppScreen, vmId: String? = null) {
        if (vmId != null) {
            _selectedVmId.value = vmId
            val vm = allVms.value.find { it.id == vmId }
            if (vm != null) {
                engine.attachVm(vm)
            }
        }
        _currentScreen.value = screen
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setDistroFilter(filter: String) {
        _selectedDistroFilter.value = filter
    }

    fun toggleViewMode() {
        _isGridView.value = !_isGridView.value
    }

    fun selectVm(vm: VmInstance) {
        _selectedVmId.value = vm.id
        engine.attachVm(vm)
    }

    fun startVm(vm: VmInstance) {
        selectVm(vm)
        engine.startVm(vm) { status, uptime, cpu, ram ->
            repository.updateTelemetry(vm.id, status, uptime, cpu, ram)
        }
    }

    fun stopVm(vm: VmInstance, force: Boolean = false) {
        selectVm(vm)
        engine.stopVm(force) { status, uptime, cpu, ram ->
            repository.updateTelemetry(vm.id, status, uptime, cpu, ram)
        }
    }

    fun pauseVm(vm: VmInstance) {
        selectVm(vm)
        engine.pauseVm { status, uptime, cpu, ram ->
            repository.updateTelemetry(vm.id, status, uptime, cpu, ram)
        }
    }

    fun resumeVm(vm: VmInstance) {
        selectVm(vm)
        engine.resumeVm { status, uptime, cpu, ram ->
            repository.updateTelemetry(vm.id, status, uptime, cpu, ram)
        }
    }

    fun rebootVm(vm: VmInstance) {
        selectVm(vm)
        engine.rebootVm { status, uptime, cpu, ram ->
            repository.updateTelemetry(vm.id, status, uptime, cpu, ram)
        }
    }

    fun bootIsoLive(iso: IsoImage) {
        viewModelScope.launch {
            val distro = DistroCatalog.getById(iso.distroId) ?: DistroCatalog.templates.first()
            val newVm = VmInstance(
                id = UUID.randomUUID().toString(),
                name = "${distro.name} Live ISO",
                distroId = distro.id,
                distroName = distro.name,
                architecture = iso.architecture,
                vCpuCores = 2,
                ramMb = distro.recommendedRamMb,
                diskSizeGb = distro.defaultDiskGb,
                status = VmStatus.STOPPED,
                bootMode = BootMode.ISO_BOOT,
                isoPath = iso.localUri,
                displayMode = "GUI_DESKTOP",
                gpuDriver = "VIRGL_3D",
                notes = "Live ISO boot session launched directly from ISO Library."
            )
            repository.insertVm(newVm)
            startVm(newVm)
            navigateTo(AppScreen.VM_DISPLAY, newVm.id)
        }
    }

    fun createVm(
        name: String,
        distroId: String,
        arch: String,
        cores: Int,
        ramMb: Int,
        diskGb: Int,
        bootMode: BootMode,
        gpuDriver: String,
        lowLatency: Boolean,
        kvm: Boolean,
        isoPath: String?
    ) {
        viewModelScope.launch {
            val template = DistroCatalog.getById(distroId) ?: DistroCatalog.templates.first()
            val newVm = VmInstance(
                id = UUID.randomUUID().toString(),
                name = name.ifBlank { "${template.name} Virtual Machine" },
                distroId = template.id,
                distroName = template.name,
                architecture = arch,
                vCpuCores = cores,
                ramMb = ramMb,
                diskSizeGb = diskGb,
                status = VmStatus.STOPPED,
                bootMode = bootMode,
                isoPath = isoPath ?: if (bootMode == BootMode.ISO_BOOT) "virtu://images/${template.isoFileName}" else null,
                diskFormat = "QCOW2",
                displayMode = "GUI_DESKTOP",
                gpuDriver = gpuDriver,
                lowLatencyPreempt = lowLatency,
                kvmAccelerated = kvm,
                notes = "Created via VirtuLinux hypervisor wizard."
            )
            repository.insertVm(newVm)
        }
    }

    fun deleteVm(vm: VmInstance) {
        viewModelScope.launch {
            if (vm.status == VmStatus.RUNNING) {
                engine.stopVm(force = true)
            }
            repository.deleteVm(vm.id)
            if (_selectedVmId.value == vm.id) {
                _selectedVmId.value = allVms.value.firstOrNull { it.id != vm.id }?.id
            }
        }
    }

    fun cloneVm(vm: VmInstance) {
        viewModelScope.launch {
            val cloned = vm.copy(
                id = UUID.randomUUID().toString(),
                name = "${vm.name} (Clone)",
                status = VmStatus.STOPPED,
                uptimeSeconds = 0L,
                cpuUsagePercent = 0f,
                ramUsageMb = 0,
                createdAt = System.currentTimeMillis()
            )
            repository.insertVm(cloned)
        }
    }

    fun takeSnapshot(vm: VmInstance, name: String) {
        viewModelScope.launch {
            val snapshot = VmSnapshot(
                id = UUID.randomUUID().toString(),
                vmId = vm.id,
                name = name.ifBlank { "Snapshot ${System.currentTimeMillis() % 10000}" },
                ramAllocatedMb = vm.ramMb,
                diskSizeGb = vm.diskSizeGb,
                description = "Point-in-time hypervisor snapshot"
            )
            repository.createSnapshot(snapshot)
        }
    }

    fun importCustomIso(name: String, fileName: String, sizeMb: Int, arch: String) {
        viewModelScope.launch {
            val newIso = IsoImage(
                id = "custom-${UUID.randomUUID()}",
                name = name,
                fileName = fileName,
                distroId = "custom-distro",
                sizeMb = sizeMb,
                architecture = arch,
                isDownloaded = true,
                localUri = "file:///storage/emulated/0/Download/$fileName",
                isCustomUserIso = true
            )
            repository.insertIso(newIso)
        }
    }

    fun deleteIso(iso: IsoImage) {
        viewModelScope.launch {
            repository.deleteIso(iso.id)
        }
    }

    // Storage and file management methods
    fun setStoragePath(path: String) {
        _currentStoragePath.value = path
    }

    fun openFileForEditing(file: VirtualFile) {
        _activeEditingFile.value = file
    }

    fun closeEditingFile() {
        _activeEditingFile.value = null
    }

    fun saveActiveFile(content: String) {
        val file = _activeEditingFile.value ?: return
        engine.fileSystem.updateFileContent(file.path, content)
        _activeEditingFile.value = engine.fileSystem.getFile(file.path)
    }

    fun createNewFileInStorage(name: String, content: String = "") {
        engine.fileSystem.createFile(_currentStoragePath.value, name, content)
    }

    fun createNewFolderInStorage(name: String) {
        engine.fileSystem.createDirectory(_currentStoragePath.value, name)
    }

    fun deleteFileFromStorage(path: String) {
        engine.fileSystem.deleteFile(path)
        if (_activeEditingFile.value?.path == path) {
            _activeEditingFile.value = null
        }
    }
}
