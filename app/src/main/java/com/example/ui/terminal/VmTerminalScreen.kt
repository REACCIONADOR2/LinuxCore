package com.example.ui.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VmStatus
import com.example.ui.MainViewModel
import com.example.ui.components.KeyboardKeyButton
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AccentRed
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun VmTerminalScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentVm by viewModel.currentVm.collectAsState()
    val engine = viewModel.engine
    val vmStatus by engine.status.collectAsState()
    val termHistory by engine.terminalHistory.collectAsState()
    val currentPath by engine.currentPath.collectAsState()
    val latency by engine.guestLatencyMs.collectAsState()

    var cmdInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    LaunchedEffect(termHistory.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    val vm = currentVm

    if (vm == null) {
        Box(modifier = modifier.fillMaxSize().background(CarbonDark), contentAlignment = Alignment.Center) {
            Text("No Virtual Machine selected", color = TextSecondary, fontFamily = FontFamily.Monospace)
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CarbonDark)
            .testTag("vm_terminal_screen")
    ) {
        // Terminal Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CarbonSurface)
                .border(0.5.dp, CarbonBorder)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = TerminalGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "ttyS0 • ${vm.name}",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Serial Port 115200 8N1 (PREEMPT_RT: ${String.format("%.1f", latency)}ms)",
                        color = NeonCyan,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(vmStatus)
                Spacer(modifier = Modifier.width(8.dp))
                if (vmStatus == VmStatus.RUNNING) {
                    IconButton(
                        onClick = { viewModel.stopVm(vm) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Stop",
                            tint = AccentRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = { viewModel.startVm(vm) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start",
                            tint = TerminalGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        if (vmStatus != VmStatus.RUNNING) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Virtual Machine is ${vmStatus.name}",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Start the VM to establish interactive serial TTY terminal connection.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.startVm(vm) },
                        colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Connect & Boot", color = CarbonDark, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        } else {
            // Interactive Terminal Screen Body
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(CarbonDark)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
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
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }
            }

            // Quick Shell Command Bar
            val quickCommands = listOf(
                "neofetch",
                "htop",
                "uname -a",
                "ls -la",
                "df -h",
                "free -m",
                "ip addr",
                "ping 1.1.1.1",
                "dmesg",
                "cat /etc/os-release",
                "clear"
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CarbonSurface)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                items(quickCommands) { cmd ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CarbonSurfaceVariant)
                            .border(0.5.dp, CarbonBorder, RoundedCornerShape(6.dp))
                            .clickable { engine.executeShellCommand(cmd) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = cmd,
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Extended Key Toolbar (Esc, Tab, Ctrl+C, Ctrl+Z, Pipe, Sudo, Redirection)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CarbonDark)
                    .border(0.5.dp, CarbonBorder)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                KeyboardKeyButton("Esc", onClick = { /* noop */ })
                KeyboardKeyButton("Tab", onClick = { engine.executeShellCommand("ls") })
                KeyboardKeyButton("Ctrl+C", onClick = { engine.executeShellCommand("^C") }, isHighlighted = true)
                KeyboardKeyButton("Ctrl+Z", onClick = { engine.executeShellCommand("^Z") })
                KeyboardKeyButton("|", onClick = { cmdInput += " | " })
                KeyboardKeyButton("~", onClick = { cmdInput += "~/" })
                KeyboardKeyButton("/", onClick = { cmdInput += "/" })
                KeyboardKeyButton(">", onClick = { cmdInput += " > " })
            }

            // Command Input Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CarbonSurface)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "user@virtulinux:${currentPath}\$ ",
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
                        .testTag("terminal_input_field"),
                    singleLine = true
                )
                IconButton(
                    onClick = {
                        if (cmdInput.isNotBlank()) {
                            engine.executeShellCommand(cmdInput)
                            cmdInput = ""
                        }
                    },
                    modifier = Modifier.size(32.dp).testTag("terminal_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = TerminalGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
