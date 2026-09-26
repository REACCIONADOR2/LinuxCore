package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.VmSnapshot

@Entity(tableName = "vm_snapshots")
data class SnapshotEntity(
    @PrimaryKey val id: String,
    val vmId: String,
    val name: String,
    val timestamp: Long,
    val ramAllocatedMb: Int,
    val diskSizeGb: Int,
    val description: String
) {
    fun toDomain(): VmSnapshot = VmSnapshot(
        id = id,
        vmId = vmId,
        name = name,
        timestamp = timestamp,
        ramAllocatedMb = ramAllocatedMb,
        diskSizeGb = diskSizeGb,
        description = description
    )

    companion object {
        fun fromDomain(model: VmSnapshot): SnapshotEntity = SnapshotEntity(
            id = model.id,
            vmId = model.vmId,
            name = model.name,
            timestamp = model.timestamp,
            ramAllocatedMb = model.ramAllocatedMb,
            diskSizeGb = model.diskSizeGb,
            description = model.description
        )
    }
}
