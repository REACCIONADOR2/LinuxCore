package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.BootMode
import com.example.data.model.VmInstance
import com.example.data.model.VmStatus

@Entity(tableName = "virtual_machines")
data class VmEntity(
    @PrimaryKey val id: String,
    val name: String,
    val distroId: String,
    val distroName: String,
    val architecture: String,
    val vCpuCores: Int,
    val ramMb: Int,
    val diskSizeGb: Int,
    val status: String,
    val bootMode: String,
    val isoPath: String?,
    val diskFormat: String,
    val displayMode: String,
    val gpuDriver: String,
    val lowLatencyPreempt: Boolean,
    val kvmAccelerated: Boolean,
    val memoryBallooning: Boolean,
    val networkType: String,
    val portForwarding: String,
    val uptimeSeconds: Long,
    val cpuUsagePercent: Float,
    val ramUsageMb: Int,
    val createdAt: Long,
    val lastBootTime: Long,
    val notes: String
) {
    fun toDomain(): VmInstance {
        return VmInstance(
            id = id,
            name = name,
            distroId = distroId,
            distroName = distroName,
            architecture = architecture,
            vCpuCores = vCpuCores,
            ramMb = ramMb,
            diskSizeGb = diskSizeGb,
            status = try { VmStatus.valueOf(status) } catch (e: Exception) { VmStatus.STOPPED },
            bootMode = try { BootMode.valueOf(bootMode) } catch (e: Exception) { BootMode.VIRTUAL_DISK },
            isoPath = isoPath,
            diskFormat = diskFormat,
            displayMode = displayMode,
            gpuDriver = gpuDriver,
            lowLatencyPreempt = lowLatencyPreempt,
            kvmAccelerated = kvmAccelerated,
            memoryBallooning = memoryBallooning,
            networkType = networkType,
            portForwarding = portForwarding,
            uptimeSeconds = uptimeSeconds,
            cpuUsagePercent = cpuUsagePercent,
            ramUsageMb = ramUsageMb,
            createdAt = createdAt,
            lastBootTime = lastBootTime,
            notes = notes
        )
    }

    companion object {
        fun fromDomain(model: VmInstance): VmEntity {
            return VmEntity(
                id = model.id,
                name = model.name,
                distroId = model.distroId,
                distroName = model.distroName,
                architecture = model.architecture,
                vCpuCores = model.vCpuCores,
                ramMb = model.ramMb,
                diskSizeGb = model.diskSizeGb,
                status = model.status.name,
                bootMode = model.bootMode.name,
                isoPath = model.isoPath,
                diskFormat = model.diskFormat,
                displayMode = model.displayMode,
                gpuDriver = model.gpuDriver,
                lowLatencyPreempt = model.lowLatencyPreempt,
                kvmAccelerated = model.kvmAccelerated,
                memoryBallooning = model.memoryBallooning,
                networkType = model.networkType,
                portForwarding = model.portForwarding,
                uptimeSeconds = model.uptimeSeconds,
                cpuUsagePercent = model.cpuUsagePercent,
                ramUsageMb = model.ramUsageMb,
                createdAt = model.createdAt,
                lastBootTime = model.lastBootTime,
                notes = model.notes
            )
        }
    }
}
