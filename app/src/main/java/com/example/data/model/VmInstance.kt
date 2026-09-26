package com.example.data.model

enum class VmStatus {
    STOPPED,
    BOOTING,
    RUNNING,
    PAUSED,
    ERROR
}

enum class BootMode {
    ISO_BOOT,
    VIRTUAL_DISK,
    DIRECT_KERNEL
}

data class VmInstance(
    val id: String,
    val name: String,
    val distroId: String,
    val distroName: String,
    val architecture: String = "x86_64", // x86_64, aarch64, riscv64
    val vCpuCores: Int = 2,
    val ramMb: Int = 2048,
    val diskSizeGb: Int = 20,
    val status: VmStatus = VmStatus.STOPPED,
    val bootMode: BootMode = BootMode.VIRTUAL_DISK,
    val isoPath: String? = null,
    val diskFormat: String = "QCOW2", // QCOW2, RAW
    val displayMode: String = "GUI_DESKTOP", // GUI_DESKTOP, TERMINAL_ONLY
    val gpuDriver: String = "VIRGL_3D", // VIRGL_3D, VIRTIO_GPU, QXL, LLVMPIPE
    val lowLatencyPreempt: Boolean = true,
    val kvmAccelerated: Boolean = true,
    val memoryBallooning: Boolean = true,
    val networkType: String = "NAT_USER", // NAT_USER, TAP_BRIDGED, ISOLATED
    val portForwarding: String = "2222:22,8080:80",
    val uptimeSeconds: Long = 0L,
    val cpuUsagePercent: Float = 0f,
    val ramUsageMb: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastBootTime: Long = 0L,
    val notes: String = ""
)
