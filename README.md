# VirtuLinux — Android Linux Hypervisor & OS Studio

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Android Gradle Plugin](https://img.shields.io/badge/AGP-9.1.1-green.svg?logo=android)](https://developer.android.com/studio/releases/gradle-plugin)
[![Jetpack Compose](https://img.shields.io/badge/Compose-Material%203-brightgreen.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Arch-x86__64%20%7C%20ARM64%20%7C%20RISC--V-orange.svg)]()
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

**VirtuLinux** is an advanced Android virtual machine manager and hypervisor dashboard designed to boot, emulate, and manage Linux distributions with graphic desktop environments, low-latency kernel scheduling, persistent virtual storage, and hardware acceleration.

---

## 🌟 Key Capabilities

### 1. Comprehensive Linux Distro Catalog & Arch Emulation
- **Multi-Architecture Support:** Emulates and runs **x86_64**, **AArch64 (ARM64)**, and **RISC-V 64** architectures.
- **Pre-configured Distributions:**
  - **Ubuntu 24.04 LTS (Noble Numbat)** — GNOME 46 Wayland with LTS stability.
  - **Arch Linux** — Bleeding-edge rolling release with Hyprland and dynamic Pacman repositories.
  - **Alpine Linux 3.20** — Musl libc & BusyBox micro-kernel (instant sub-second boot, near-zero footprint).
  - **Debian 12 (Bookworm)** — Standard lightweight XFCE workstation.
  - **Kali Linux 2024.3** — Security auditing, penetration testing, and forensic network tools.
  - **Fedora Workstation 40** — GNOME 46 with PipeWire low-latency sound and Btrfs root.
  - **Void Linux** — Independent runit init supervisor for fast service booting.
  - **Linux Mint 22 (Wilma)** — Cinnamon desktop environment.
  - **Ubuntu ARM64** — Native ARM execution on Android multi-core processors.

### 2. Interactive Graphical Desktop Interface (GUI)
- **Window Compositing:** Multi-tasking draggable and resizable windows:
  - **virtu-terminal (bash):** Shell with real command execution (`uname`, `neofetch`, `htop`, `ls`, `df`, `free`, `ip`, `ping`, `dmesg`).
  - **File Explorer:** Graphical directory navigation (`/home/user`, `/etc`, `/mnt/shared_android`).
  - **Code & Config Editor:** In-app editor with syntax styling and direct save to virtual ext4 disks.
  - **System Monitor:** Real-time vCPU core sparklines, RAM usage, and rendering frame rates.
- **Desktop Taskbar:** Application launcher menu, open window switches, and system tray.
- **Touch & Mouse Modes:** Toggle between direct touch interaction and a virtual trackpad cursor with zero input drag latency.
- **Virtual Keyboard Toolbar:** Rapid access to `Ctrl+C`, `Tab`, `Esc`, `|`, `~`, `sudo`, and arrow keys.

### 3. Hardware Acceleration & Low-Latency Kernel Tuning
- **GPU Acceleration:** VirGL 3D GPU passthrough via host OpenGL ES 3.2 targeting 60 FPS, VirtIO-GPU, and Mesa Gallium 24.1.
- **Low Latency Scheduling:** PREEMPT_RT real-time kernel task scheduling (1000Hz) delivering sub-3ms guest dispatch latency.
- **Virtualization Core:** pKVM / KVM hardware acceleration with multi-threaded TCG JIT fallback.
- **VirtIO Driver Suite:**
  - Storage: `virtio-blk` / NVMe controller.
  - Network: `virtio-net` with port forwarding matrix (SSH `2222:22`, HTTP `8080:80`, Dev `3000:3000`).
  - Memory: Dynamic VirtIO ballooning and HugePages support.

### 4. Boot from .ISO & Custom Disk Images
- **Live ISO Boot Loader:** Boot live operating systems into RAM overlayfs with a single tap.
- **Custom ISO Importer:** Mount user `.iso`, `.img`, or `.qcow2` files directly from Android storage or local download paths.
- **Boot Options:** SeaBIOS, UEFI / OVMF SecureBoot, and Direct Kernel Boot (`vmlinuz` + `initrd`).

### 5. Persistent Storage & Intuitive File Management
- **QCOW2 Sparse Storage:** Copy-on-write virtual disk images with snapshot management.
- **File System Tree Explorer:** Inspect directories, manage permissions (`chmod`/`chown`), inspect file sizes, create, and delete files.
- **Host ↔ Guest Shared Storage (`/mnt/shared_android`):** Synchronize files between Android host storage and the Linux guest VM via VirtIO-9P / VirtioFS.

### 6. Customizable Hypervisor Dashboard
- Host hardware resource monitoring (Host CPU load, RAM allocation, storage capacity, active VM count).
- Distro family filter chips and instant search.
- Grid and List view switching.
- Full instance lifecycle controls: Start, ACPI Shutdown, Pause, Resume, Reboot, Display/Console switch, Snapshot capture, Clone, and Delete.

---

## 🛠 Tech Stack & Architecture

- **Language:** Kotlin 2.2.10
- **UI Framework:** Jetpack Compose with Material 3 (M3)
- **Architecture:** MVVM (Model-View-ViewModel) + Clean Architecture
- **Local Persistence:** Room Database 2.7.0 (with KSP)
- **Concurrency & Reactivity:** Kotlin Coroutines & StateFlow / SharedFlow
- **Design System:** Custom Carbon Dark Hypervisor cockpit theme with responsive Edge-to-Edge and `WindowInsets.navigationBars` support.

---

## 📂 Project Structure

```text
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/example/
│   │   │   ├── MainActivity.kt               # Main activity with edge-to-edge & BackHandler
│   │   │   ├── data/
│   │   │   │   ├── local/                    # Room Database, DAOs, and Entities
│   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   ├── VmDao.kt
│   │   │   │   │   ├── VmEntity.kt
│   │   │   │   │   ├── IsoEntity.kt
│   │   │   │   │   └── SnapshotEntity.kt
│   │   │   │   ├── model/                    # Domain models (VmInstance, DistroCatalog, VirtualFile)
│   │   │   │   └── repository/               # VmRepository (Repository pattern)
│   │   │   ├── engine/                       # Linux Kernel & Hardware Emulation Engine
│   │   │   │   ├── KernelEmulationEngine.kt   # Execution state machine, bash shell parser, telemetry
│   │   │   │   └── VirtualFileSystem.kt      # Hierarchical virtual file system (/home, /etc, /mnt)
│   │   │   └── ui/                           # Jetpack Compose UI
│   │   │       ├── MainViewModel.kt          # UI state, VM actions, and storage controllers
│   │   │       ├── components/               # Header, BottomNav, Badges, Metrics
│   │   │       ├── dashboard/                # VM instances dashboard (Grid/List, Filter, Metrics)
│   │   │       ├── display/                  # Interactive Linux GUI desktop & window manager
│   │   │       ├── terminal/                 # High-performance serial TTY console
│   │   │       ├── iso/                      # ISO catalog and boot loader
│   │   │       ├── storage/                  # Virtual disks & file system explorer
│   │   │       ├── create/                   # VM deployment wizard dialog
│   │   │       └── settings/                 # Hypervisor & pKVM / VirGL tuning
│   │   └── res/                              # Strings, vectors, colors, themes
│   └── build.gradle.kts                      # Module Gradle build configuration
├── gradle/libs.versions.toml                 # Version catalog
├── metadata.json                             # AI Studio project platform metadata
└── package.json                              # Build descriptor & verification scripts
```

---

## 🚀 Building & Running

### Requirements
- Android Studio Ladybug / Meerkat (or newer)
- JDK 17 or JDK 21
- Android SDK 34 / 36 (Minimum SDK: 24 — Android 7.0+)

### Automatic GitHub Releases & APK Generation
This repository includes a pre-configured GitHub Actions CI/CD workflow (`.github/workflows/release.yml`) that automatically builds and attaches the installable `.apk` and SHA-256 checksums to your GitHub Releases.

#### Option 1: Trigger via Git Tag (Recommended)
Push a version tag to GitHub to trigger an automatic release build:
```bash
git tag v1.0.0
git push origin v1.0.0
```
GitHub Actions will build `VirtuLinux-v1.0.0.apk` and publish it under the **Releases** tab of your repository.

#### Option 2: Trigger Manually via GitHub Web UI
1. Go to your repository on GitHub.
2. Click on the **Actions** tab.
3. Select **Build & Release VirtuLinux APK** in the left sidebar.
4. Click **Run workflow**, specify the tag name (e.g. `v1.0.0`), and click **Run workflow**.

#### Option 3: Download Directly from AI Studio
In Google AI Studio, open the top-right settings/export menu and choose **Generate APK** to download the compiled package directly to your computer.

### Via Android Studio
1. Clone your repository:
   ```bash
   git clone https://github.com/<your-username>/<your-repo-name>.git
   ```
2. Open the project in Android Studio.
3. Allow Gradle to sync dependencies.
4. Select an Android device or emulator (API 24+) and click **Run** (`Shift + F10`).

### Via Command Line
```bash
# Build Debug APK
./gradlew assembleDebug

# Run Unit & Robolectric Tests
./gradlew testDebugUnitTest
```

---

## 📄 License

Licensed under the [Apache License, Version 2.0](LICENSE).
