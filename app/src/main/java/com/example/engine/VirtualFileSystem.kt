package com.example.engine

import com.example.data.model.VirtualFile

class VirtualFileSystem {
    private val files = mutableMapOf<String, VirtualFile>()

    init {
        resetToDefault()
    }

    fun resetToDefault() {
        files.clear()
        // Standard root directories
        addDir("/", "")
        addDir("/bin", "bin")
        addDir("/boot", "boot")
        addDir("/dev", "dev")
        addDir("/etc", "etc")
        addDir("/home", "home")
        addDir("/home/user", "user")
        addDir("/home/user/Desktop", "Desktop")
        addDir("/home/user/Documents", "Documents")
        addDir("/home/user/Downloads", "Downloads")
        addDir("/mnt", "mnt")
        addDir("/mnt/shared_android", "shared_android", isHostShared = true)
        addDir("/proc", "proc")
        addDir("/root", "root")
        addDir("/sys", "sys")
        addDir("/tmp", "tmp")
        addDir("/usr", "usr")
        addDir("/usr/bin", "bin")
        addDir("/var", "var")
        addDir("/var/log", "log")

        // Standard configuration & system files
        addFile(
            "/etc/os-release",
            "os-release",
            """
            NAME="VirtuLinux Guest"
            PRETTY_NAME="VirtuLinux Accelerated Linux Kernel 6.8"
            ID=virtulinux
            ID_LIKE="arch debian fedora"
            VERSION="2024.09 LTS"
            VERSION_CODENAME=accelerated
            HOME_URL="https://virtulinux.internal"
            """.trimIndent()
        )

        addFile(
            "/etc/hostname",
            "hostname",
            "virtulinux-vm\n"
        )

        addFile(
            "/etc/hosts",
            "hosts",
            """
            127.0.0.1   localhost
            127.0.1.1   virtulinux-vm.localdomain virtulinux-vm
            192.168.122.1 gateway.virtu.internal host.android
            """.trimIndent()
        )

        addFile(
            "/etc/fstab",
            "fstab",
            """
            # /etc/fstab: static file system information.
            /dev/vda1   /               ext4    errors=remount-ro,noatime 0 1
            /dev/vda2   none            swap    sw                        0 0
            virtfs_shared /mnt/shared_android 9p trans=virtio,version=9p2000.L 0 0
            """.trimIndent()
        )

        // Home directory user files
        addFile(
            "/home/user/.bashrc",
            ".bashrc",
            """
            # ~/.bashrc: executed by bash(1) for non-login shells.
            export PS1='\[\033[01;32m\]user@virtulinux\[\033[00m\]:\[\033[01;34m\]\w\[\033[00m\]\$ '
            export PATH=/usr/local/bin:/usr/bin:/bin:/usr/local/games
            export TERM=xterm-256color
            export LIBGL_ALWAYS_SOFTWARE=0
            export MESA_LOADER_DRIVER_OVERRIDE=virgl
            alias ll='ls -la --color=auto'
            alias cls='clear'
            """.trimIndent()
        )

        addFile(
            "/home/user/Desktop/welcome.txt",
            "welcome.txt",
            """
            =====================================================
            Welcome to VirtuLinux - High-Performance OS Hypervisor!
            =====================================================
            Features active:
            - VirGL 3D Hardware Accelerated GPU (OpenGL ES passthrough)
            - Low Latency PREEMPT_RT Realtime Kernel Task Scheduler
            - VirtIO-Net low-overhead network stack (slirp user-mode)
            - Persistent QCOW2 sparse virtual storage
            - Host-to-guest shared folder at /mnt/shared_android
            
            Enjoy native-speed Linux execution directly on Android!
            """.trimIndent()
        )

        addFile(
            "/home/user/Documents/benchmark.sh",
            "benchmark.sh",
            """
            #!/bin/bash
            echo "[+] Starting VirtuLinux Kernel Benchmark..."
            echo "[*] Kernel: $(uname -r)"
            echo "[*] Architecture: $(uname -m)"
            echo "[*] CPU vCores: $(nproc)"
            echo "[*] GPU Driver: VirGL 3D Accelerated"
            echo "[+] Running matrix multiplication computation..."
            echo "[✓] Score: 9840 MFLOPS (Near-native ARM/x86 JIT throughput)"
            """.trimIndent(),
            permissions = "-rwxr-xr-x"
        )

        addFile(
            "/mnt/shared_android/readme_shared.md",
            "readme_shared.md",
            """
            # Android Host <-> Linux Guest Shared Storage
            Files placed in this directory are synchronized with the host Android
            file system via VirtIO-9P / VirtioFS with near-zero latency.
            """.trimIndent(),
            isHostShared = true
        )

        addFile(
            "/var/log/dmesg",
            "dmesg",
            """
            [    0.000000] Linux version 6.8.0-virtulinux (gcc version 13.2.0) #1 SMP PREEMPT_RT
            [    0.000004] Command line: BOOT_IMAGE=/vmlinuz-6.8.0 root=/dev/vda1 rw console=tty0
            [    0.001204] KVM: Hardware assisted virtualization enabled
            [    0.008432] SMP: Allowing 8 CPUs, 4 hotplug CPUs
            [    0.012543] Memory: 3072MB available (persistent qcow2 backing)
            [    0.045210] pci 0000:00:01.0: [1af4:1050] VirtIO GPU device detected
            [    0.045890] virgl 3d: Hardware acceleration activated via host OpenGL ES 3.2
            [    0.051200] virtio_net: Link up, 10000Mbps full duplex
            [    0.071200] virtio_blk: [vda] 41943040 512-byte logical blocks (21.4 GB)
            [    0.089400] EXT4-fs (vda1): mounted filesystem with ordered data mode
            [    0.120000] systemd[1]: Reached target Graphical Interface.
            """.trimIndent()
        )
    }

    private fun addDir(path: String, name: String, isHostShared: Boolean = false) {
        files[path] = VirtualFile(
            name = name.ifEmpty { "/" },
            path = path,
            isDirectory = true,
            permissions = "drwxr-xr-x",
            isHostShared = isHostShared
        )
    }

    private fun addFile(
        path: String,
        name: String,
        content: String,
        permissions: String = "-rw-r--r--",
        isHostShared: Boolean = false
    ) {
        val size = content.toByteArray().size.toLong()
        files[path] = VirtualFile(
            name = name,
            path = path,
            isDirectory = false,
            sizeBytes = size,
            permissions = permissions,
            content = content,
            isHostShared = isHostShared
        )
    }

    fun getFile(path: String): VirtualFile? = files[path]

    fun listFiles(dirPath: String): List<VirtualFile> {
        val normalized = if (dirPath == "/") "/" else dirPath.trimEnd('/')
        return files.values.filter { file ->
            if (file.path == normalized) return@filter false
            val parentPath = getParentPath(file.path)
            parentPath == normalized
        }.sortedWith(compareBy({ !it.isDirectory }, { it.name }))
    }

    fun getAllFiles(): List<VirtualFile> = files.values.toList()

    fun createDirectory(parentPath: String, name: String): Boolean {
        val normParent = if (parentPath == "/") "" else parentPath.trimEnd('/')
        val fullPath = "$normParent/$name"
        if (files.containsKey(fullPath)) return false
        addDir(fullPath, name)
        return true
    }

    fun createFile(parentPath: String, name: String, content: String): Boolean {
        val normParent = if (parentPath == "/") "" else parentPath.trimEnd('/')
        val fullPath = "$normParent/$name"
        addFile(fullPath, name, content)
        return true
    }

    fun updateFileContent(path: String, newContent: String): Boolean {
        val existing = files[path] ?: return false
        files[path] = existing.copy(
            content = newContent,
            sizeBytes = newContent.toByteArray().size.toLong(),
            lastModified = "Just now"
        )
        return true
    }

    fun deleteFile(path: String): Boolean {
        if (path == "/" || path == "/bin" || path == "/etc" || path == "/home") return false
        val removed = files.remove(path) != null
        if (removed) {
            // Also remove children if directory
            val prefix = "$path/"
            val childrenKeys = files.keys.filter { it.startsWith(prefix) }
            childrenKeys.forEach { files.remove(it) }
        }
        return removed
    }

    private fun getParentPath(path: String): String {
        if (path == "/") return ""
        val lastSlash = path.lastIndexOf('/')
        return if (lastSlash <= 0) "/" else path.substring(0, lastSlash)
    }
}
