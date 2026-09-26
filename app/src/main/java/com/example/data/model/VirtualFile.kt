package com.example.data.model

data class VirtualFile(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val sizeBytes: Long = 0L,
    val permissions: String = "-rw-r--r--",
    val owner: String = "user",
    val group: String = "user",
    val lastModified: String = "Sep 25 20:30",
    val content: String = "",
    val isHostShared: Boolean = false
) {
    val displaySize: String
        get() {
            if (isDirectory) return "<DIR>"
            if (sizeBytes < 1024) return "${sizeBytes} B"
            if (sizeBytes < 1024 * 1024) return "${sizeBytes / 1024} KB"
            return "${sizeBytes / (1024 * 1024)} MB"
        }
}

data class IsoImage(
    val id: String,
    val name: String,
    val fileName: String,
    val distroId: String,
    val sizeMb: Int,
    val architecture: String,
    val isDownloaded: Boolean = true,
    val downloadProgressPercent: Int = 100,
    val localUri: String = "file:///storage/emulated/0/VirtuLinux/isos/",
    val sha256Checksum: String = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    val isCustomUserIso: Boolean = false
)

data class VmSnapshot(
    val id: String,
    val vmId: String,
    val name: String,
    val timestamp: Long = System.currentTimeMillis(),
    val ramAllocatedMb: Int,
    val diskSizeGb: Int,
    val description: String = "Snapshot captured during active runtime"
)

data class HypervisorHostStats(
    val totalRamMb: Int = 8192,
    val usedRamMb: Int = 3450,
    val cpuCores: Int = 8,
    val cpuLoadPercent: Float = 24.5f,
    val hostKvmAvailable: Boolean = true,
    val activeVmsCount: Int = 0,
    val totalStorageGb: Int = 128,
    val usedStorageGb: Int = 42
)
