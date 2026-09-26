package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BootMode
import com.example.data.model.DistroCatalog
import com.example.data.model.VmInstance
import com.example.data.model.VmStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [VmEntity::class, IsoEntity::class, SnapshotEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vmDao(): VmDao
    abstract fun isoDao(): IsoDao
    abstract fun snapshotDao(): SnapshotDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "virtulinux_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.vmDao(), database.isoDao())
                }
            }
        }

        private suspend fun populateInitialData(vmDao: VmDao, isoDao: IsoDao) {
            // Seed ISO images from catalog
            val defaultIsos = DistroCatalog.templates.map { template ->
                IsoEntity(
                    id = "iso-${template.id}",
                    name = "${template.name} Live ISO",
                    fileName = template.isoFileName,
                    distroId = template.id,
                    sizeMb = template.isoSizeMb,
                    architecture = template.architecture,
                    isDownloaded = true,
                    downloadProgressPercent = 100,
                    localUri = "virtu://images/${template.isoFileName}",
                    sha256Checksum = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08",
                    isCustomUserIso = false
                )
            }
            isoDao.insertIsos(defaultIsos)

            // Seed 3 ready-to-run VMs
            val alpineVm = VmInstance(
                id = UUID.randomUUID().toString(),
                name = "Alpine Micro Kernel (Ultra Low Latency)",
                distroId = "alpine-musl",
                distroName = "Alpine Linux 3.20",
                architecture = "x86_64",
                vCpuCores = 2,
                ramMb = 1024,
                diskSizeGb = 8,
                status = VmStatus.STOPPED,
                bootMode = BootMode.VIRTUAL_DISK,
                isoPath = "virtu://images/alpine-standard-3.20.3-x86_64.iso",
                diskFormat = "QCOW2",
                displayMode = "GUI_DESKTOP",
                gpuDriver = "VIRGL_3D",
                lowLatencyPreempt = true,
                kvmAccelerated = true,
                memoryBallooning = true,
                networkType = "NAT_USER",
                portForwarding = "2222:22,8080:80",
                notes = "Pre-configured ultra-low latency Musl kernel for fast microservice testing."
            )

            val archVm = VmInstance(
                id = UUID.randomUUID().toString(),
                name = "Arch Linux Hyprland Workstation",
                distroId = "arch-rolling",
                distroName = "Arch Linux",
                architecture = "x86_64",
                vCpuCores = 4,
                ramMb = 3072,
                diskSizeGb = 25,
                status = VmStatus.STOPPED,
                bootMode = BootMode.VIRTUAL_DISK,
                isoPath = "virtu://images/archlinux-2024.09.01-x86_64.iso",
                diskFormat = "QCOW2",
                displayMode = "GUI_DESKTOP",
                gpuDriver = "VIRGL_3D",
                lowLatencyPreempt = true,
                kvmAccelerated = true,
                memoryBallooning = true,
                networkType = "NAT_USER",
                portForwarding = "2223:22,3000:3000",
                notes = "Dynamic Wayland composited desktop with full Pacman repo access."
            )

            val debianVm = VmInstance(
                id = UUID.randomUUID().toString(),
                name = "Debian 12 Bookworm XFCE",
                distroId = "debian-12",
                distroName = "Debian 12 Bookworm",
                architecture = "x86_64",
                vCpuCores = 2,
                ramMb = 2048,
                diskSizeGb = 16,
                status = VmStatus.STOPPED,
                bootMode = BootMode.VIRTUAL_DISK,
                isoPath = "virtu://images/debian-live-12.7.0-amd64-xfce.iso",
                diskFormat = "RAW",
                displayMode = "GUI_DESKTOP",
                gpuDriver = "VIRTIO_GPU",
                lowLatencyPreempt = false,
                kvmAccelerated = true,
                memoryBallooning = true,
                networkType = "NAT_USER",
                portForwarding = "2224:22,8000:80",
                notes = "Rock solid standard Linux environment with persistent home and /etc configuration."
            )

            vmDao.insertVm(VmEntity.fromDomain(alpineVm))
            vmDao.insertVm(VmEntity.fromDomain(archVm))
            vmDao.insertVm(VmEntity.fromDomain(debianVm))
        }
    }
}
