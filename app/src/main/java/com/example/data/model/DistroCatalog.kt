package com.example.data.model

data class DistroTemplate(
    val id: String,
    val name: String,
    val family: String, // Debian, Arch, RedHat, Alpine, Independent
    val version: String,
    val defaultDesktop: String, // GNOME, XFCE, KDE, Hyprland, Musl/Sway, CLI
    val architecture: String, // x86_64, aarch64, riscv64
    val minRamMb: Int,
    val recommendedRamMb: Int,
    val defaultDiskGb: Int,
    val packageManager: String, // apt, pacman, dnf, apk, xbps
    val isoFileName: String,
    val isoSizeMb: Int,
    val description: String,
    val kernelVersion: String,
    val isLiveBootReady: Boolean = true,
    val defaultUser: String = "user",
    val defaultPassword: String = "virtu"
)

object DistroCatalog {
    val templates: List<DistroTemplate> = listOf(
        DistroTemplate(
            id = "ubuntu-24-lts",
            name = "Ubuntu 24.04 LTS",
            family = "Debian",
            version = "24.04.1 (Noble Numbat)",
            defaultDesktop = "GNOME 46 Wayland",
            architecture = "x86_64",
            minRamMb = 2048,
            recommendedRamMb = 4096,
            defaultDiskGb = 25,
            packageManager = "apt",
            isoFileName = "ubuntu-24.04.1-desktop-amd64.iso",
            isoSizeMb = 4600,
            description = "The world's most widely used Linux workstation distro with LTS enterprise stability and full hardware acceleration.",
            kernelVersion = "6.8.0-40-generic"
        ),
        DistroTemplate(
            id = "arch-rolling",
            name = "Arch Linux",
            family = "Arch",
            version = "Rolling 2024.09",
            defaultDesktop = "Hyprland / Wayland",
            architecture = "x86_64",
            minRamMb = 1024,
            recommendedRamMb = 2048,
            defaultDiskGb = 20,
            packageManager = "pacman",
            isoFileName = "archlinux-2024.09.01-x86_64.iso",
            isoSizeMb = 980,
            description = "Bleeding-edge rolling release with dynamic Pacman packages and hyper-responsive modern Wayland compositing.",
            kernelVersion = "6.10.9-arch1-1"
        ),
        DistroTemplate(
            id = "alpine-musl",
            name = "Alpine Linux 3.20",
            family = "Alpine",
            version = "3.20.3 (Standard)",
            defaultDesktop = "XFCE / Musl Lightweight",
            architecture = "x86_64",
            minRamMb = 512,
            recommendedRamMb = 1024,
            defaultDiskGb = 8,
            packageManager = "apk",
            isoFileName = "alpine-standard-3.20.3-x86_64.iso",
            isoSizeMb = 215,
            description = "Ultra-low latency Musl & BusyBox powered micro-kernel distro. Boots in under 1 second with near-zero memory footprint.",
            kernelVersion = "6.6.51-0-lts"
        ),
        DistroTemplate(
            id = "debian-12",
            name = "Debian 12 Bookworm",
            family = "Debian",
            version = "12.7 (Bookworm)",
            defaultDesktop = "XFCE 4.18",
            architecture = "x86_64",
            minRamMb = 1024,
            recommendedRamMb = 2048,
            defaultDiskGb = 16,
            packageManager = "apt",
            isoFileName = "debian-live-12.7.0-amd64-xfce.iso",
            isoSizeMb = 2800,
            description = "The universal rock-solid operating system with maximum package compatibility and ultra-efficient resource management.",
            kernelVersion = "6.1.0-25-amd64"
        ),
        DistroTemplate(
            id = "kali-rolling",
            name = "Kali Linux",
            family = "Debian",
            version = "2024.3 (Security Auditing)",
            defaultDesktop = "XFCE 4.18 Stealth",
            architecture = "x86_64",
            minRamMb = 2048,
            recommendedRamMb = 4096,
            defaultDiskGb = 30,
            packageManager = "apt",
            isoFileName = "kali-linux-2024.3-live-amd64.iso",
            isoSizeMb = 3900,
            description = "Premier cyber security and penetration testing platform loaded with 600+ network inspection and kernel tools.",
            kernelVersion = "6.8.11-kali1"
        ),
        DistroTemplate(
            id = "fedora-40",
            name = "Fedora Workstation 40",
            family = "RedHat",
            version = "40.1.14",
            defaultDesktop = "GNOME 46 / PipeWire",
            architecture = "x86_64",
            minRamMb = 2048,
            recommendedRamMb = 4096,
            defaultDiskGb = 25,
            packageManager = "dnf",
            isoFileName = "Fedora-Workstation-Live-x86_64-40-1.14.iso",
            isoSizeMb = 2300,
            description = "Leading-edge desktop Linux showcasing Wayland native gestures, PipeWire low-latency sound, and Btrfs root subvolumes.",
            kernelVersion = "6.8.5-301.fc40.x86_64"
        ),
        DistroTemplate(
            id = "void-linux",
            name = "Void Linux",
            family = "Independent",
            version = "20240314",
            defaultDesktop = "XFCE / Runit",
            architecture = "x86_64",
            minRamMb = 512,
            recommendedRamMb = 1024,
            defaultDiskGb = 10,
            packageManager = "xbps",
            isoFileName = "void-live-x86_64-20240314-xfce.iso",
            isoSizeMb = 980,
            description = "Independent distribution built from scratch with runit init supervisor for instantaneous system services and XBPS package speed.",
            kernelVersion = "6.6.21_1"
        ),
        DistroTemplate(
            id = "linux-mint",
            name = "Linux Mint 22",
            family = "Debian",
            version = "22.0 (Wilma)",
            defaultDesktop = "Cinnamon 6.2",
            architecture = "x86_64",
            minRamMb = 2048,
            recommendedRamMb = 4096,
            defaultDiskGb = 25,
            packageManager = "apt",
            isoFileName = "linuxmint-22-cinnamon-64bit.iso",
            isoSizeMb = 2900,
            description = "Refined, comfortable user experience with Cinnamon desktop, smooth window compositing, and comprehensive driver manager.",
            kernelVersion = "6.8.0-38-generic"
        ),
        DistroTemplate(
            id = "arm-ubuntu",
            name = "Ubuntu ARM64 Server/GUI",
            family = "Debian",
            version = "24.04 LTS (AArch64)",
            defaultDesktop = "Ubuntu Desktop ARM",
            architecture = "aarch64",
            minRamMb = 1536,
            recommendedRamMb = 3072,
            defaultDiskGb = 20,
            packageManager = "apt",
            isoFileName = "ubuntu-24.04-live-server-arm64.iso",
            isoSizeMb = 2600,
            description = "Native ARM64 architecture VM running directly on your Android processor cores with near-native CPU cycle execution.",
            kernelVersion = "6.8.0-arm64"
        )
    )

    fun getById(id: String): DistroTemplate? = templates.find { it.id == id }
}
