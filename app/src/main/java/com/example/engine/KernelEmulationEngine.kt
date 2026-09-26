package com.example.engine

import com.example.data.model.BootMode
import com.example.data.model.DistroCatalog
import com.example.data.model.VmInstance
import com.example.data.model.VmStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

data class KernelLog(
    val timestamp: String,
    val level: String, // INFO, WARN, KERNEL, SYSTEM
    val message: String
)

data class VmProcess(
    val pid: Int,
    val user: String,
    val cpuPercent: Float,
    val memPercent: Float,
    val time: String,
    val command: String
)

data class GuiWindowState(
    val id: String,
    val title: String,
    val appType: String, // TERMINAL, FILE_MANAGER, TEXT_EDITOR, SYSTEM_MONITOR, SETTINGS
    val isOpen: Boolean = true,
    val isMinimized: Boolean = false,
    val isMaximized: Boolean = false,
    val zIndex: Int = 1
)

class KernelEmulationEngine(
    private val scope: CoroutineScope
) {
    val fileSystem = VirtualFileSystem()

    private val _currentVm = MutableStateFlow<VmInstance?>(null)
    val currentVm: StateFlow<VmInstance?> = _currentVm.asStateFlow()

    private val _status = MutableStateFlow(VmStatus.STOPPED)
    val status: StateFlow<VmStatus> = _status.asStateFlow()

    private val _bootProgress = MutableStateFlow(0f)
    val bootProgress: StateFlow<Float> = _bootProgress.asStateFlow()

    private val _bootStageText = MutableStateFlow("Hypervisor Ready")
    val bootStageText: StateFlow<String> = _bootStageText.asStateFlow()

    private val _kernelLogs = MutableStateFlow<List<KernelLog>>(emptyList())
    val kernelLogs: StateFlow<List<KernelLog>> = _kernelLogs.asStateFlow()

    private val _terminalHistory = MutableStateFlow<List<String>>(emptyList())
    val terminalHistory: StateFlow<List<String>> = _terminalHistory.asStateFlow()

    private val _currentPath = MutableStateFlow("/home/user")
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    private val _fpsCounter = MutableStateFlow(60)
    val fpsCounter: StateFlow<Int> = _fpsCounter.asStateFlow()

    private val _guestLatencyMs = MutableStateFlow(2.4f)
    val guestLatencyMs: StateFlow<Float> = _guestLatencyMs.asStateFlow()

    private val _openWindows = MutableStateFlow<List<GuiWindowState>>(
        listOf(
            GuiWindowState(id = "win-term", title = "virtu-terminal ~ bash", appType = "TERMINAL", zIndex = 2),
            GuiWindowState(id = "win-sys", title = "System Resource Monitor", appType = "SYSTEM_MONITOR", isMinimized = true, zIndex = 1)
        )
    )
    val openWindows: StateFlow<List<GuiWindowState>> = _openWindows.asStateFlow()

    private var executionJob: Job? = null
    private var telemetryJob: Job? = null
    private var uptimeSeconds = 0L

    fun attachVm(vm: VmInstance) {
        _currentVm.value = vm
        _status.value = vm.status
    }

    fun startVm(vm: VmInstance, onStatusChange: (suspend (VmStatus, Long, Float, Int) -> Unit)? = null) {
        _currentVm.value = vm
        executionJob?.cancel()
        telemetryJob?.cancel()

        executionJob = scope.launch(Dispatchers.Default) {
            _status.value = VmStatus.BOOTING
            _bootProgress.value = 0.05f
            _kernelLogs.value = emptyList()

            val distro = DistroCatalog.getById(vm.distroId) ?: DistroCatalog.templates.first()
            val arch = vm.architecture
            val isIso = vm.bootMode == BootMode.ISO_BOOT || vm.isoPath != null

            addKernelLog("0.000000", "KERNEL", "VirtuLinux QEMU/KVM Hypervisor v9.1.0 (Android Engine)")
            addKernelLog("0.000002", "INFO", "Accelerated Execution Mode: ${if (vm.kvmAccelerated) "Hardware KVM (pKVM)" else "Multi-Threaded TCG JIT"}")
            addKernelLog("0.000005", "INFO", "Allocating ${vm.vCpuCores} vCPUs, ${vm.ramMb} MB RAM, Scheduler: PREEMPT_RT Low Latency")
            _bootStageText.value = "Initializing SeaBIOS & UEFI SecureBoot..."
            _bootProgress.value = 0.20f
            delay(120)

            if (isIso) {
                addKernelLog("0.021000", "INFO", "ISO Boot Device Mounted: ${vm.isoPath ?: distro.isoFileName}")
                addKernelLog("0.025000", "INFO", "Booting ISO Live Environment in RAM overlayfs...")
            } else {
                addKernelLog("0.021000", "INFO", "Probing Virtual Disk /dev/vda (${vm.diskFormat} ${vm.diskSizeGb}GB, VirtIO-BLK)")
            }

            _bootStageText.value = "Decompressing Linux Kernel ${distro.kernelVersion}..."
            _bootProgress.value = 0.45f
            addKernelLog("0.045000", "KERNEL", "Linux version ${distro.kernelVersion} (SMP PREEMPT_RT) on $arch")
            addKernelLog("0.052000", "INFO", "VirtIO GPU: Driver initialized with ${vm.gpuDriver} acceleration")
            addKernelLog("0.054000", "INFO", "virgl3d: OpenGL ES 3.2 passthrough active (target 60+ FPS)")
            addKernelLog("0.058000", "INFO", "virtio_net: Link speed 10 Gbps (NAT 192.168.122.42/24)")
            addKernelLog("0.062000", "INFO", "virtio_9p: Host shared folder mounted at /mnt/shared_android")
            delay(150)

            _bootStageText.value = "Starting System Services (${distro.packageManager.uppercase()})..."
            _bootProgress.value = 0.75f
            addKernelLog("0.089000", "SYSTEM", "udevd[24]: initialized virtual devices in 12ms")
            addKernelLog("0.110000", "SYSTEM", "dbus-daemon[45]: system bus started")
            addKernelLog("0.134000", "SYSTEM", "sshd[58]: listening on port 22 (forwarded to host)")
            delay(130)

            _bootStageText.value = "Starting ${distro.defaultDesktop}..."
            _bootProgress.value = 1.0f
            addKernelLog("0.180000", "SYSTEM", "Wayland/X11 Compositor active on /dev/dri/card0")
            addKernelLog("0.192000", "INFO", "Guest session ready. Low latency loop engaged.")

            _status.value = VmStatus.RUNNING
            _bootStageText.value = "Running (${distro.name})"
            uptimeSeconds = 0L

            initTerminalGreeting(vm, distro)

            startTelemetryLoop(vm, onStatusChange)
        }
    }

    private fun startTelemetryLoop(vm: VmInstance, onStatusChange: (suspend (VmStatus, Long, Float, Int) -> Unit)?) {
        telemetryJob = scope.launch(Dispatchers.Default) {
            while (isActive && _status.value == VmStatus.RUNNING) {
                delay(1000)
                uptimeSeconds++
                // Realistic dynamic CPU fluctuations
                val baseCpu = if (vm.displayMode == "GUI_DESKTOP") 8.0f else 3.0f
                val jitter = Random.nextFloat() * 12.0f
                val cpu = (baseCpu + jitter).coerceIn(1.0f, 95.0f)
                val ram = (vm.ramMb * 0.32f + Random.nextInt(20, 80)).toInt().coerceAtMost(vm.ramMb)
                val fps = (58 + Random.nextInt(0, 5)).coerceAtMost(62)
                val latency = (2.1f + Random.nextFloat() * 1.2f)

                _fpsCounter.value = fps
                _guestLatencyMs.value = latency

                onStatusChange?.invoke(VmStatus.RUNNING, uptimeSeconds, cpu, ram)
            }
        }
    }

    fun pauseVm(onStatusChange: (suspend (VmStatus, Long, Float, Int) -> Unit)? = null) {
        telemetryJob?.cancel()
        _status.value = VmStatus.PAUSED
        _bootStageText.value = "Paused (vCPU Execution Halted)"
        addKernelLog("PAUSE", "KERNEL", "Hypervisor intercepted pause: vCPU registers frozen.")
        scope.launch {
            onStatusChange?.invoke(VmStatus.PAUSED, uptimeSeconds, 0f, 0)
        }
    }

    fun resumeVm(onStatusChange: (suspend (VmStatus, Long, Float, Int) -> Unit)? = null) {
        val vm = _currentVm.value ?: return
        _status.value = VmStatus.RUNNING
        _bootStageText.value = "Running"
        addKernelLog("RESUME", "KERNEL", "Hypervisor resumed execution: vCPU clock running.")
        startTelemetryLoop(vm, onStatusChange)
    }

    fun stopVm(force: Boolean = false, onStatusChange: (suspend (VmStatus, Long, Float, Int) -> Unit)? = null) {
        telemetryJob?.cancel()
        executionJob?.cancel()
        scope.launch(Dispatchers.Default) {
            if (!force) {
                addKernelLog("ACPI", "SYSTEM", "ACPI Power Button pressed. Sending SIGTERM to all processes...")
                _bootStageText.value = "Shutting down..."
                delay(180)
                addKernelLog("ACPI", "SYSTEM", "Unmounting /dev/vda1 and syncing buffers.")
            }
            _status.value = VmStatus.STOPPED
            _bootStageText.value = "Poweroff (Hypervisor Idle)"
            uptimeSeconds = 0L
            onStatusChange?.invoke(VmStatus.STOPPED, 0L, 0f, 0)
        }
    }

    fun rebootVm(onStatusChange: (suspend (VmStatus, Long, Float, Int) -> Unit)? = null) {
        val vm = _currentVm.value ?: return
        stopVm(force = true)
        scope.launch {
            delay(300)
            startVm(vm, onStatusChange)
        }
    }

    private fun addKernelLog(ts: String, level: String, msg: String) {
        val updated = _kernelLogs.value.toMutableList()
        updated.add(KernelLog(ts, level, msg))
        _kernelLogs.value = updated
    }

    private fun initTerminalGreeting(vm: VmInstance, distro: com.example.data.model.DistroTemplate) {
        _terminalHistory.value = listOf(
            "Linux virtulinux 6.8.0-virtulinux #1 SMP PREEMPT_RT ${vm.architecture}",
            "Welcome to ${distro.name} (${distro.version}) on VirtuLinux Hypervisor",
            " * Documentation: https://wiki.virtulinux.org",
            " * Low Latency Engine: PREEMPT_RT Active (latency: ~2.4ms)",
            " * GPU Driver: ${vm.gpuDriver} with host OpenGL ES 3.2 Passthrough",
            " * Storage: /dev/vda mounted on / (${vm.diskFormat} ${vm.diskSizeGb}GB)",
            "",
            "Type 'help' or 'neofetch' to view system specs. Type 'ls' to explore.",
            ""
        )
    }

    fun executeShellCommand(cmdRaw: String): String {
        val cmd = cmdRaw.trim()
        if (cmd.isEmpty()) return ""

        val history = _terminalHistory.value.toMutableList()
        val prompt = "user@virtulinux:${_currentPath.value}\$ $cmd"
        history.add(prompt)

        val parts = cmd.split("\\s+".toRegex()).filter { it.isNotBlank() }
        val program = parts.firstOrNull() ?: ""
        val args = parts.drop(1)

        val output: String = when (program) {
            "help" -> """
                VirtuLinux Shell Utilities:
                  uname [-a]        Print kernel and architecture information
                  neofetch          Show graphic system information & logo
                  htop / top        Display active processes & CPU/RAM load
                  ls [-la] [dir]    List directory contents
                  cd [dir]          Change directory
                  pwd               Print working directory
                  cat [file]        View file contents
                  touch [file]      Create empty file
                  mkdir [dir]       Create new folder
                  rm [-rf] [path]   Remove file or directory
                  echo [text]       Display text or redirect to file
                  df [-h]           Report file system disk space
                  free [-m]         Display memory usage statistics
                  ip [addr]         Display network interfaces and IP
                  ping [host]       Send network packets with latency test
                  dmesg             Print kernel ring buffer logs
                  clear             Clear terminal screen
                  reboot            Reboot virtual machine
                  poweroff          ACPI power down VM
            """.trimIndent()

            "clear" -> {
                _terminalHistory.value = emptyList()
                return ""
            }

            "pwd" -> _currentPath.value

            "uname" -> {
                val vm = _currentVm.value
                val arch = vm?.architecture ?: "x86_64"
                if (args.contains("-a") || args.contains("-r")) {
                    "Linux virtulinux 6.8.0-virtulinux #1 SMP PREEMPT_RT ${arch} GNU/Linux"
                } else {
                    "Linux"
                }
            }

            "neofetch" -> generateNeofetchOutput()

            "htop", "top" -> generateProcessList()

            "free" -> {
                val vm = _currentVm.value
                val total = vm?.ramMb ?: 2048
                val used = (total * 0.35f).toInt()
                val free = total - used
                """
                           total        used        free      shared  buff/cache   available
                Mem:        $total         $used        $free          12         450        $free
                Swap:       2048           0        2048
                """.trimIndent()
            }

            "df" -> {
                val vm = _currentVm.value
                val totalGb = vm?.diskSizeGb ?: 20
                val usedGb = 4
                val availGb = totalGb - usedGb
                """
                Filesystem     1K-blocks      Used Available Use% Mounted on
                /dev/vda1       ${totalGb * 1048576}   4194304  ${availGb * 1048576}  20% /
                tmpfs             524288         0    524288   0% /dev/shm
                virtfs_shared    8388608   1048576   7340032  13% /mnt/shared_android
                """.trimIndent()
            }

            "ip" -> {
                """
                1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN
                    inet 127.0.0.1/8 scope host lo
                2: eth0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc fq_codel state UP
                    inet 192.168.122.42/24 brd 192.168.122.255 scope global dynamic eth0
                    inet6 fe80::5054:ff:fe12:3456/64 scope link
                """.trimIndent()
            }

            "ping" -> {
                val host = args.firstOrNull() ?: "1.1.1.1"
                """
                PING $host ($host) 56(84) bytes of data.
                64 bytes from $host: icmp_seq=1 ttl=118 time=1.84 ms
                64 bytes from $host: icmp_seq=2 ttl=118 time=1.92 ms
                64 bytes from $host: icmp_seq=3 ttl=118 time=1.75 ms
                --- $host ping statistics ---
                3 packets transmitted, 3 received, 0% packet loss, time 2003ms
                rtt min/avg/max/mdev = 1.75/1.83/1.92/0.07 ms (low-latency virtio_net)
                """.trimIndent()
            }

            "cd" -> {
                val target = args.firstOrNull() ?: "/home/user"
                val resolved = resolvePath(target)
                val targetFile = fileSystem.getFile(resolved)
                if (targetFile != null && targetFile.isDirectory) {
                    _currentPath.value = resolved
                    ""
                } else {
                    "bash: cd: $target: No such file or directory"
                }
            }

            "ls" -> {
                val target = args.firstOrNull { !it.startsWith("-") }
                val targetDir = if (target != null) resolvePath(target) else _currentPath.value
                val isDetailed = args.any { it.contains("l") }
                val showAll = args.any { it.contains("a") }

                val files = fileSystem.listFiles(targetDir)
                    .filter { showAll || !it.name.startsWith(".") }

                if (files.isEmpty()) {
                    ""
                } else if (isDetailed) {
                    val header = "total ${files.size * 4}\n"
                    header + files.joinToString("\n") { file ->
                        val dirMarker = if (file.isDirectory) "d" else "-"
                        val perms = if (file.permissions.startsWith("-") || file.permissions.startsWith("d"))
                            file.permissions
                        else
                            "$dirMarker${file.permissions}"
                        val sizeStr = file.displaySize.padStart(8)
                        "$perms 1 ${file.owner} ${file.group} $sizeStr ${file.lastModified} ${file.name}"
                    }
                } else {
                    files.joinToString("  ") { file ->
                        if (file.isDirectory) "${file.name}/" else file.name
                    }
                }
            }

            "cat" -> {
                val fileArg = args.firstOrNull()
                if (fileArg == null) {
                    "cat: missing file operand"
                } else {
                    val resolved = resolvePath(fileArg)
                    val f = fileSystem.getFile(resolved)
                    if (f == null) {
                        "cat: $fileArg: No such file or directory"
                    } else if (f.isDirectory) {
                        "cat: $fileArg: Is a directory"
                    } else {
                        f.content
                    }
                }
            }

            "touch" -> {
                val fileName = args.firstOrNull()
                if (fileName == null) {
                    "touch: missing file operand"
                } else {
                    fileSystem.createFile(_currentPath.value, fileName, "")
                    ""
                }
            }

            "mkdir" -> {
                val dirName = args.firstOrNull()
                if (dirName == null) {
                    "mkdir: missing operand"
                } else {
                    val success = fileSystem.createDirectory(_currentPath.value, dirName)
                    if (!success) "mkdir: cannot create directory '$dirName': File exists" else ""
                }
            }

            "rm" -> {
                val target = args.lastOrNull { !it.startsWith("-") }
                if (target == null) {
                    "rm: missing operand"
                } else {
                    val resolved = resolvePath(target)
                    val success = fileSystem.deleteFile(resolved)
                    if (!success) "rm: cannot remove '$target': No such file or directory" else ""
                }
            }

            "echo" -> {
                if (args.contains(">")) {
                    val splitIdx = args.indexOf(">")
                    val text = args.take(splitIdx).joinToString(" ").removeSurrounding("\"")
                    val fileName = args.getOrNull(splitIdx + 1)
                    if (fileName != null) {
                        fileSystem.createFile(_currentPath.value, fileName, text)
                        ""
                    } else {
                        "bash: syntax error near unexpected token 'newline'"
                    }
                } else {
                    args.joinToString(" ").removeSurrounding("\"")
                }
            }

            "whoami" -> "user"
            "date" -> "Fri Sep 25 20:45:00 UTC 2026"

            "dmesg" -> {
                _kernelLogs.value.takeLast(15).joinToString("\n") {
                    "[ ${it.timestamp.padStart(10)} ] [${it.level}] ${it.message}"
                }
            }

            "reboot" -> {
                rebootVm()
                "Broadcast message from root@virtulinux: The system is going down for reboot NOW!"
            }

            "poweroff", "shutdown" -> {
                stopVm()
                "Broadcast message from root@virtulinux: The system is going down for poweroff NOW!"
            }

            "apt", "pacman", "apk", "dnf" -> {
                val action = args.firstOrNull() ?: "help"
                "[*] ${program.uppercase()}: Package cache up to date. Verified repository mirrors at 192.168.122.1.\n[✓] Virtual software library synchronized."
            }

            else -> "bash: $program: command not found"
        }

        if (output.isNotBlank()) {
            history.addAll(output.lines())
        }
        history.add("")
        _terminalHistory.value = history
        return output
    }

    private fun resolvePath(input: String): String {
        return when {
            input == "~" || input == "" -> "/home/user"
            input.startsWith("~/") -> "/home/user/" + input.removePrefix("~/")
            input.startsWith("/") -> input
            input == ".." -> {
                val current = _currentPath.value
                val lastSlash = current.lastIndexOf('/')
                if (lastSlash <= 0) "/" else current.substring(0, lastSlash)
            }
            input == "." -> _currentPath.value
            else -> {
                val curr = _currentPath.value.trimEnd('/')
                "$curr/$input"
            }
        }
    }

    private fun generateNeofetchOutput(): String {
        val vm = _currentVm.value
        val distro = DistroCatalog.getById(vm?.distroId ?: "") ?: DistroCatalog.templates.first()
        val arch = vm?.architecture ?: "x86_64"
        val ram = vm?.ramMb ?: 2048

        return """
                   -`                    user@virtulinux
                  .o+`                   ---------------
                 `ooo/                   OS: ${distro.name} $arch
                `+oooo:                  Host: VirtuLinux QEMU/KVM Hypervisor
               `+oooooo:                 Kernel: ${distro.kernelVersion} (PREEMPT_RT)
               -+oooooo+:                Uptime: ${uptimeSeconds / 60}m ${uptimeSeconds % 60}s
             `/:-:++oooo+:               Packages: 842 (${distro.packageManager})
            `/++++/+++++++:              Shell: bash 5.2.26
           `/++++++++++++++:             Resolution: 1920x1080 (60Hz)
          `/+++ooooooooooooo/`           DE: ${distro.defaultDesktop}
         ./ooosssso++osssssso+`          WM: Mutter / XFWM Compositor
        .oossssso-````/ossssss+`         GPU: ${vm?.gpuDriver ?: "VIRGL_3D"} (OpenGL ES 3.2 Passthrough)
       -osssssso.      :ssssssso.        vCPUs: ${vm?.vCpuCores ?: 2} cores
      :osssssss/        osssso+++.       Memory: 720MiB / ${ram}MiB
     /ossssssss/        +ssssooo/-
   `/ossssso+/:-        -:/+osssso+-     Latency: ${_guestLatencyMs.value}ms (Low-latency scheduler)
        """.trimIndent()
    }

    private fun generateProcessList(): String {
        return """
          PID USER      PR  NI    VIRT    RES    SHR S  %CPU  %MEM     TIME+ COMMAND
            1 root      20   0  168540  12480   8400 S   0.0   0.4   0:01.42 systemd
           24 root      20   0   45120   6200   4900 S   0.0   0.2   0:00.18 systemd-udevd
           45 messageb  20   0    9400   3800   3200 S   0.0   0.1   0:00.08 dbus-daemon
           58 root      20   0   14200   5400   4400 S   0.0   0.2   0:00.04 sshd
          112 user      20   0  324500  48200  32000 S   2.4   2.1   0:04.12 Xwayland / Xorg
          145 user      20   0  182000  28400  21000 S   1.8   1.2   0:02.50 desktop-session
          230 user      20   0   15200   7200   4800 S   0.5   0.3   0:00.90 bash
          340 user      20   0   18400   6100   3900 R   4.2   0.3   0:00.12 htop
        """.trimIndent()
    }

    // Window Management methods for Graphic Interface
    fun toggleWindow(appType: String, title: String) {
        val current = _openWindows.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.appType == appType }
        if (existingIndex >= 0) {
            val win = current[existingIndex]
            if (win.isMinimized) {
                current[existingIndex] = win.copy(isMinimized = false, zIndex = current.maxOfOrNull { it.zIndex }?.plus(1) ?: 1)
            } else {
                current[existingIndex] = win.copy(isMinimized = true)
            }
        } else {
            val newZ = (current.maxOfOrNull { it.zIndex } ?: 0) + 1
            current.add(GuiWindowState(id = "win-$appType", title = title, appType = appType, zIndex = newZ))
        }
        _openWindows.value = current
    }

    fun closeWindow(appType: String) {
        _openWindows.value = _openWindows.value.filter { it.appType != appType }
    }

    fun bringWindowToFront(appType: String) {
        val current = _openWindows.value.toMutableList()
        val idx = current.indexOfFirst { it.appType == appType }
        if (idx >= 0) {
            val maxZ = (current.maxOfOrNull { it.zIndex } ?: 0) + 1
            current[idx] = current[idx].copy(zIndex = maxZ, isMinimized = false)
            _openWindows.value = current
        }
    }
}
