package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.IsoEntity
import com.example.data.local.SnapshotEntity
import com.example.data.local.VmEntity
import com.example.data.model.IsoImage
import com.example.data.model.VmInstance
import com.example.data.model.VmSnapshot
import com.example.data.model.VmStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VmRepository(private val database: AppDatabase) {
    private val vmDao = database.vmDao()
    private val isoDao = database.isoDao()
    private val snapshotDao = database.snapshotDao()

    val allVms: Flow<List<VmInstance>> = vmDao.getAllVms().map { entities ->
        entities.map { it.toDomain() }
    }

    val allIsos: Flow<List<IsoImage>> = isoDao.getAllIsos().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getVmById(id: String): Flow<VmInstance?> = vmDao.getVmByIdFlow(id).map { it?.toDomain() }

    fun getSnapshotsForVm(vmId: String): Flow<List<VmSnapshot>> =
        snapshotDao.getSnapshotsForVm(vmId).map { entities -> entities.map { it.toDomain() } }

    suspend fun insertVm(vm: VmInstance) {
        vmDao.insertVm(VmEntity.fromDomain(vm))
    }

    suspend fun updateVm(vm: VmInstance) {
        vmDao.updateVm(VmEntity.fromDomain(vm))
    }

    suspend fun updateVmStatus(id: String, status: VmStatus) {
        val vm = vmDao.getVmById(id) ?: return
        val updated = vm.copy(
            status = status.name,
            lastBootTime = if (status == VmStatus.RUNNING) System.currentTimeMillis() else vm.lastBootTime
        )
        vmDao.updateVm(updated)
    }

    suspend fun updateTelemetry(id: String, status: VmStatus, uptime: Long, cpu: Float, ram: Int) {
        vmDao.updateVmTelemetry(id, status.name, uptime, cpu, ram)
    }

    suspend fun deleteVm(id: String) {
        vmDao.deleteVmById(id)
    }

    suspend fun insertIso(iso: IsoImage) {
        isoDao.insertIso(IsoEntity.fromDomain(iso))
    }

    suspend fun deleteIso(id: String) {
        isoDao.deleteIsoById(id)
    }

    suspend fun createSnapshot(snapshot: VmSnapshot) {
        snapshotDao.insertSnapshot(SnapshotEntity.fromDomain(snapshot))
    }

    suspend fun deleteSnapshot(id: String) {
        snapshotDao.deleteSnapshotById(id)
    }
}
