package com.example.ui.display

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DistroCatalog
import com.example.data.model.VirtualFile
import com.example.data.model.VmInstance
import com.example.data.model.VmStatus
import com.example.engine.GuiWindowState
import com.example.ui.MainViewModel
import com.example.ui.components.KeyboardKeyButton
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentRed
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceVariant
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun VmDisplayScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentVm by viewModel.currentVm.collectAsState()
    val engine = viewModel.engine
    val vmStatus by engine.status.collectAsState()
    val bootProgress by engine.bootProgress.collectAsState()
    val bootStageText by engine.bootStageText.collectAsState()
    val fps by engine.fpsCounter.collectAsState()
    val latency by engine.guestLatencyMs.collectAsState()
    val openWindows by engine.openWindows.collectAsState()
    val termHistory by engine.terminalHistory.collectAsState()
    val currentPath by engine.currentPath.collectAsState()

    var showAppMenu by remember { mutableStateOf(false) }
    var showVirtualKeyboardToolbar by remember { mutableStateOf(true) }
    var trackpadMode by remember { mutableStateOf(false) }
    var cursorX by remember { mutableFloatStateOf(160f) }
    var cursorY by remember { mutableFloatStateOf(160f) }

    val vm = currentVm

    if (vm == null) {
        Box(
            modifier = modifier.fillMaxSize().background(CarbonDark),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No Virtual Machine selected",
                color = TextSecondary,
                fontFamily = FontFamily.Monospace
            )
        }
        return
    }

    val distro = DistroCatalog.getById(vm.distroId) ?: DistroCatalog.templates.first()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CarbonDark)
            .testTag("vm_display_screen")
    ) {
        // Display Top Bar
        DisplayTopBar(
            vm = vm,
            distroName = distro.name,
            status = vmStatus,
            fps = fps,
            latency = latency,
            trackpadMode = trackpadMode,
            onToggleTrackpad = { trackpadMode = !trackpadMode },
            onToggleKeyboard = { showVirtualKeyboardToolbar = !showVirtualKeyboardToolbar },
            onReboot = { viewModel.rebootVm(vm) },
            onStop = { viewModel.stopVm(vm) },
            onStart = { viewModel.startVm(vm) }
        )

        // Main Graphical Display Canvas
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(CarbonDark)
        ) {
            val canvasWidth = maxWidth
            val canvasHeight = maxHeight

            when (vmStatus) {
                VmStatus.BOOTING -> {
                    // Booting Animation & Splash
                    BootingSplashView(
                        vm = vm,
                        distro = distro,
                        progress = bootProgress,
                        stage = bootStageText,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                VmStatus.STOPPED, VmStatus.ERROR -> {
                    // Stopped View
                    StoppedDisplayView(
                        vm = vm,
                        onStart = { viewModel.startVm(vm) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                VmStatus.RUNNING, VmStatus.PAUSED -> {
                    // Live Desktop Environment
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        when (distro.family) {
                                            "Arch" -> Color(0xFF172B3C)
                                            "Debian" -> Color(0xFF331628)
                                            "RedHat" -> Color(0xFF142B40)
                                            "Alpine" -> Color(0xFF152636)
                                            else -> Color(0xFF182234)
                                        },
                                        CarbonDark
                                    )
                                )
                            )
                            .pointerInput(trackpadMode) {
                                if (trackpadMode) {
                                    detectDragGestures { _, dragAmount ->
                                        cursorX = (cursorX + dragAmount.x).coerceIn(0f, size.width.toFloat() - 16f)
                                        cursorY = (cursorY + dragAmount.y).coerceIn(0f, size.height.toFloat() - 16f)
                                    }
                                }
                            }
                    ) {
                        // Desktop Watermark / Branding
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = distro.name.uppercase(),
                                color = Color.White.copy(alpha = 0.08f),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 4.sp
                            )
                            Text(
                                text = "VirGL 3D ACCELERATED • PREEMPT_RT",
                                color = Color.White.copy(alpha = 0.05f),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Windows Rendering
                        openWindows.sortedBy { it.zIndex }.forEach { windowState ->
                            if (!windowState.isMinimized) {
                                GuiWindowComposable(
                                    windowState = windowState,
                                    engine = engine,
                                    vm = vm,
                                    currentPath = currentPath,
                                    termHistory = termHistory,
                                    onClose = { engine.closeWindow(windowState.appType) },
                                    onFocus = { engine.bringWindowToFront(windowState.appType) },
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }

                        // Mouse pointer overlay in trackpad mode
                        if (trackpadMode) {
                            Icon(
                                imageVector = Icons.Default.Mouse,
                                contentDescription = "Cursor",
                                tint = NeonCyan,
                                modifier = Modifier
                                    .offset { IntOffset(cursorX.roundToInt(), cursorY.roundToInt()) }
                                    .size(18.dp)
                            )
                        }

                        // App Menu Pop-up
                        if (showAppMenu) {
                            DesktopAppMenu(
                                onLaunchApp = { appType, title ->
                                    engine.toggleWindow(appType, title)
                                    showAppMenu = false
                                },
                                onDismiss = { showAppMenu = false },
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(start = 8.dp, bottom = 48.dp)
                            )
                        }

                        // Linux Desktop Taskbar (Panel)
                        DesktopTaskbar(
                            distro = distro,
                            openWindows = openWindows,
                            showAppMenu = showAppMenu,
                            onToggleAppMenu = { showAppMenu = !showAppMenu },
                            onToggleWindow = { win -> engine.toggleWindow(win.appType, win.title) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }

        // Virtual Keyboard Quick Bar
        AnimatedVisibility(visible = showVirtualKeyboardToolbar && vmStatus == VmStatus.RUNNING) {
            VirtualKeyboardBar(
                onSendKey = { key ->
                    when (key) {
                        "Ctrl+C" -> engine.executeShellCommand("^C")
                        "clear" -> engine.executeShellCommand("clear")
                        "neofetch" -> engine.executeShellCommand("neofetch")
                        "htop" -> engine.toggleWindow("SYSTEM_MONITOR", "System Resource Monitor")
                        "ls" -> engine.executeShellCommand("ls -la")
                        "Tab" -> engine.executeShellCommand("ls")
                        else -> engine.executeShellCommand(key)
                    }
                }
            )
        }
    }
}

@Composable
fun DisplayTopBar(
    vm: VmInstance,
    distroName: String,
    status: VmStatus,
    fps: Int,
    latency: Float,
    trackpadMode: Boolean,
    onToggleTrackpad: () -> Unit,
    onToggleKeyboard: () -> Unit,
    onReboot: () -> Unit,
    onStop: () -> Unit,
    onStart: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CarbonSurface)
            .border(0.5.dp, CarbonBorder)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(TerminalGreen.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${vm.architecture.uppercase()} / ${vm.gpuDriver}",
                    color = TerminalGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "${fps}fps • ${String.format("%.1f", latency)}ms",
                color = NeonCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Trackpad toggle
            IconButton(
                onClick = onToggleTrackpad,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mouse,
                    contentDescription = "Toggle Trackpad Cursor",
                    tint = if (trackpadMode) TerminalGreen else TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Keyboard bar toggle
            IconButton(
                onClick = onToggleKeyboard,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = "Toggle Keyboard Toolbar",
                    tint = NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
            }

            if (status == VmStatus.RUNNING) {
                IconButton(
                    onClick = onReboot,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reboot",
                        tint = AccentAmber,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onStop,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Power Off",
                        tint = AccentRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = onStart,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start VM",
                        tint = TerminalGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BootingSplashView(
    vm: VmInstance,
    distro: com.example.data.model.DistroTemplate,
    progress: Float,
    stage: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(CarbonDark)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(TerminalGreen.copy(alpha = 0.15f))
                    .border(2.dp, TerminalGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Computer,
                    contentDescription = null,
                    tint = TerminalGreen,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = distro.name,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = "Kernel ${distro.kernelVersion} (${vm.architecture})",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(20.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = TerminalGreen,
                trackColor = CarbonSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stage,
                color = NeonCyan,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "VirGL 3D hardware acceleration engaged",
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun StoppedDisplayView(
    vm: VmInstance,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(CarbonDark)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Monitor,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "${vm.name} is Inactive",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Ready to boot with ${vm.vCpuCores} vCPUs, ${vm.ramMb}MB RAM, and ${vm.gpuDriver} GPU.",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("display_start_vm_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = CarbonDark,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Boot Linux Distro",
                    color = CarbonDark,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun GuiWindowComposable(
    windowState: GuiWindowState,
    engine: com.example.engine.KernelEmulationEngine,
    vm: VmInstance,
    currentPath: String,
    termHistory: List<String>,
    onClose: () -> Unit,
    onFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(0.96f)
            .height(290.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp))
            .clickable { onFocus() },
        colors = CardDefaults.cardColors(containerColor = CarbonSurface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Window Titlebar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CarbonSurfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(AccentRed)
                            .clickable { onClose() }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(AccentAmber)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(TerminalGreen)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = windowState.title,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Window",
                        tint = TextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // Window Content based on App Type
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(CarbonDark)
            ) {
                when (windowState.appType) {
                    "TERMINAL" -> {
                        TerminalWindowContent(
                            engine = engine,
                            currentPath = currentPath,
                            termHistory = termHistory
                        )
                    }

                    "FILE_MANAGER" -> {
                        FileManagerWindowContent(
                            engine = engine
                        )
                    }

                    "TEXT_EDITOR" -> {
                        TextEditorWindowContent(
                            engine = engine
                        )
                    }

                    "SYSTEM_MONITOR" -> {
                        SystemMonitorWindowContent(
                            vm = vm,
                            engine = engine
                        )
                    }

                    else -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Application Ready", color = TextSecondary, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TerminalWindowContent(
    engine: com.example.engine.KernelEmulationEngine,
    currentPath: String,
    termHistory: List<String>
) {
    var cmdInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            termHistory.forEach { line ->
                val color = when {
                    line.startsWith("user@virtulinux") -> TerminalGreen
                    line.startsWith("Linux") || line.startsWith("Welcome") -> NeonCyan
                    line.startsWith(" * ") -> TextSecondary
                    line.contains("error") || line.contains("not found") -> AccentRed
                    else -> TextPrimary
                }
                Text(
                    text = line,
                    color = color,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Shell command input line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CarbonSurfaceVariant)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$ ",
                color = TerminalGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            BasicTextField(
                value = cmdInput,
                onValueChange = { cmdInput = it },
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                ),
                cursorBrush = SolidColor(TerminalGreen),
                modifier = Modifier
                    .weight(1f)
                    .testTag("desktop_terminal_input"),
                singleLine = true
            )
            IconButton(
                onClick = {
                    if (cmdInput.isNotBlank()) {
                        engine.executeShellCommand(cmdInput)
                        cmdInput = ""
                    }
                },
                modifier = Modifier.size(26.dp).testTag("desktop_terminal_send")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send Command",
                    tint = TerminalGreen,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun FileManagerWindowContent(
    engine: com.example.engine.KernelEmulationEngine
) {
    var browsePath by remember { mutableStateOf("/home/user") }
    val files = remember(browsePath) { engine.fileSystem.listFiles(browsePath) }

    Column(modifier = Modifier.fillMaxSize().padding(6.dp)) {
        // Path navigation bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CarbonSurfaceVariant)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (browsePath != "/") {
                Text(
                    text = "⬆ ..",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .clickable {
                            val lastSlash = browsePath.lastIndexOf('/')
                            browsePath = if (lastSlash <= 0) "/" else browsePath.substring(0, lastSlash)
                        }
                        .padding(end = 8.dp)
                )
            }
            Text(
                text = browsePath,
                color = TextPrimary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(files) { file ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (file.isDirectory) {
                                browsePath = file.path
                            } else {
                                engine.toggleWindow("TEXT_EDITOR", "Editor: ${file.name}")
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.Code,
                            contentDescription = null,
                            tint = if (file.isDirectory) NeonCyan else TerminalGreen,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = file.name,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = file.displaySize,
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun TextEditorWindowContent(
    engine: com.example.engine.KernelEmulationEngine
) {
    var content by remember {
        mutableStateOf(
            engine.fileSystem.getFile("/home/user/Desktop/welcome.txt")?.content
                ?: "# VirtuLinux Code Editor\nprint('Hello from accelerated guest kernel!')"
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CarbonSurfaceVariant)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "welcome.txt (ext4 persistent)",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Button(
                onClick = {
                    engine.fileSystem.updateFileContent("/home/user/Desktop/welcome.txt", content)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen),
                shape = RoundedCornerShape(4.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = CarbonDark, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save", color = CarbonDark, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        }

        BasicTextField(
            value = content,
            onValueChange = { content = it },
            textStyle = TextStyle(
                color = TextPrimary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            ),
            cursorBrush = SolidColor(NeonCyan),
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
        )
    }
}

@Composable
fun SystemMonitorWindowContent(
    vm: VmInstance,
    engine: com.example.engine.KernelEmulationEngine
) {
    val latency by engine.guestLatencyMs.collectAsState()
    val fps by engine.fpsCounter.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("vCPU Core Scheduling", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            Text("${vm.vCpuCores} cores @ PREEMPT_RT", color = TerminalGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { (vm.cpuUsagePercent / 100f).coerceIn(0.05f, 0.95f) },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = TerminalGreen,
            trackColor = CarbonSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("RAM Consumption", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            Text("${vm.ramUsageMb}MB / ${vm.ramMb}MB", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { (vm.ramUsageMb.toFloat() / vm.ramMb.toFloat()).coerceIn(0.1f, 0.95f) },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = NeonCyan,
            trackColor = CarbonSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Display Render Latency: ${String.format("%.2f", latency)}ms", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text("Frame Rate: ${fps} FPS", color = TerminalGreen, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun DesktopAppMenu(
    onLaunchApp: (appType: String, title: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(220.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = "APPLICATIONS",
                color = NeonCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )

            val apps = listOf(
                Triple("TERMINAL", "Terminal (bash)", Icons.Default.Terminal),
                Triple("FILE_MANAGER", "File Manager", Icons.Default.FolderOpen),
                Triple("TEXT_EDITOR", "Text Editor", Icons.Default.Code),
                Triple("SYSTEM_MONITOR", "System Monitor", Icons.Default.Speed)
            )

            apps.forEach { (type, label, icon) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onLaunchApp(type, label) }
                        .padding(horizontal = 8.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = TerminalGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = label, color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun DesktopTaskbar(
    distro: com.example.data.model.DistroTemplate,
    openWindows: List<GuiWindowState>,
    showAppMenu: Boolean,
    onToggleAppMenu: () -> Unit,
    onToggleWindow: (GuiWindowState) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(40.dp)
            .background(CarbonSurface)
            .border(0.5.dp, CarbonBorder)
            .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Start / App Menu Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (showAppMenu) TerminalGreen else CarbonSurfaceVariant)
                    .clickable { onToggleAppMenu() }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                    .testTag("desktop_start_menu_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (showAppMenu) CarbonDark else TerminalGreen)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Virtu",
                        color = if (showAppMenu) CarbonDark else TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Task buttons for open windows
            openWindows.forEach { win ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (!win.isMinimized) NeonCyan.copy(alpha = 0.2f) else CarbonDark)
                        .border(0.5.dp, if (!win.isMinimized) NeonCyan else CarbonBorder, RoundedCornerShape(4.dp))
                        .clickable { onToggleWindow(win) }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = win.title.take(12),
                        color = if (!win.isMinimized) NeonCyan else TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
        }

        // System Tray
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Wifi, contentDescription = null, tint = TerminalGreen, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "20:45",
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun VirtualKeyboardBar(
    onSendKey: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CarbonDark)
            .border(0.5.dp, CarbonBorder)
            .padding(horizontal = 6.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf("Ctrl+C", "Tab", "ls", "neofetch", "htop", "clear", "|", "~", "/").forEach { key ->
            KeyboardKeyButton(
                label = key,
                onClick = { onSendKey(key) },
                isHighlighted = key == "Ctrl+C" || key == "neofetch"
            )
        }
    }
}
