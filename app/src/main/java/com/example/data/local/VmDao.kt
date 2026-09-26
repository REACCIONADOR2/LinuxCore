package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VmDao {
    @Query("SELECT * FROM virtual_machines ORDER BY createdAt DESC")
    fun getAllVms(): Flow<List<VmEntity>>

    @Query("SELECT * FROM virtual_machines WHERE id = :id")
    suspend fun getVmById(id: String): VmEntity?

    @Query("SELECT * FROM virtual_machines WHERE id = :id")
    fun getVmByIdFlow(id: String): Flow<VmEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVm(vm: VmEntity)

    @Update
    suspend fun updateVm(vm: VmEntity)

    @Query("UPDATE virtual_machines SET status = :status, uptimeSeconds = :uptime, cpuUsagePercent = :cpu, ramUsageMb = :ram WHERE id = :id")
    suspend fun updateVmTelemetry(id: String, status: String, uptime: Long, cpu: Float, ram: Int)

    @Query("DELETE FROM virtual_machines WHERE id = :id")
    suspend fun deleteVmById(id: String)
}

@Dao
interface IsoDao {
    @Query("SELECT * FROM iso_images ORDER BY name ASC")
    fun getAllIsos(): Flow<List<IsoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIso(iso: IsoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIsos(isos: List<IsoEntity>)

    @Query("DELETE FROM iso_images WHERE id = :id")
    suspend fun deleteIsoById(id: String)
}

@Dao
interface SnapshotDao {
    @Query("SELECT * FROM vm_snapshots WHERE vmId = :vmId ORDER BY timestamp DESC")
    fun getSnapshotsForVm(vmId: String): Flow<List<SnapshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: SnapshotEntity)

    @Query("DELETE FROM vm_snapshots WHERE id = :id")
    suspend fun deleteSnapshotById(id: String)
}
