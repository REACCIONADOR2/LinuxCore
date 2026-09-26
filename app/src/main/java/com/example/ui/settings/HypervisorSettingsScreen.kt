package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HypervisorSettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pKvmEnabled by remember { mutableStateOf(true) }
    var virGl3dEnabled by remember { mutableStateOf(true) }
    var preemptRtScheduler by remember { mutableStateOf(true) }
    var memoryBallooning by remember { mutableStateOf(true) }
    var hugePagesEnabled by remember { mutableStateOf(true) }
    var zeroCopyVirtio9p by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CarbonDark)
            .verticalScroll(rememberScrollState())
            .testTag("hypervisor_settings_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CarbonSurface)
                .border(0.5.dp, CarbonBorder)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TerminalGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Hypervisor & Kernel Tuning",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Hardware acceleration & driver configuration",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Column(modifier = Modifier.padding(14.dp)) {
            // Hardware Acceleration Card
            Text(
                text = "HARDWARE ACCELERATION (pKVM / KVM)",
                color = NeonCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, CarbonBorder, RoundedCornerShape(10.dp)),
                colors = CardDefaults.cardColors(containerColor = CarbonSurface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    SettingToggleRow(
                        title = "pKVM Hardware Virtualization",
                        subtitle = "Direct host ARM64 /dev/kvm cycle execution without JIT translation",
                        checked = pKvmEnabled,
                        onCheckedChange = { pKvmEnabled = it },
                        icon = Icons.Default.DeveloperBoard
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingToggleRow(
                        title = "VirGL 3D GPU Passthrough",
                        subtitle = "Hardware-accelerated OpenGL ES 3.2 rendering pipeline (60 FPS)",
                        checked = virGl3dEnabled,
                        onCheckedChange = { virGl3dEnabled = it },
                        icon = Icons.Default.FlashOn
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingToggleRow(
                        title = "PREEMPT_RT Realtime Scheduler",
                        subtitle = "1000Hz low-latency kernel dispatch for sub-3ms guest response",
                        checked = preemptRtScheduler,
                        onCheckedChange = { preemptRtScheduler = it },
                        icon = Icons.Default.FlashOn
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Memory & Storage Tuning
            Text(
                text = "MEMORY & VIRTUAL STORAGE",
                color = NeonCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, CarbonBorder, RoundedCornerShape(10.dp)),
                colors = CardDefaults.cardColors(containerColor = CarbonSurface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    SettingToggleRow(
                        title = "VirtIO Dynamic Memory Ballooning",
                        subtitle = "Reclaims inactive guest RAM back to Android host OS dynamically",
                        checked = memoryBallooning,
                        onCheckedChange = { memoryBallooning = it },
                        icon = Icons.Default.Memory
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingToggleRow(
                        title = "2MB HugePages Memory Support",
                        subtitle = "Reduces TLB cache misses for high performance compile workloads",
                        checked = hugePagesEnabled,
                        onCheckedChange = { hugePagesEnabled = it },
                        icon = Icons.Default.Memory
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingToggleRow(
                        title = "VirtIO-9P Host-Guest Zero Copy",
                        subtitle = "Ultra low-latency file sharing for /mnt/shared_android directory",
                        checked = zeroCopyVirtio9p,
                        onCheckedChange = { zeroCopyVirtio9p = it },
                        icon = Icons.Default.DeveloperBoard
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Network & Port Forwarding
            Text(
                text = "NETWORK & PORT FORWARDING",
                color = NeonCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, CarbonBorder, RoundedCornerShape(10.dp)),
                colors = CardDefaults.cardColors(containerColor = CarbonSurface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lan, contentDescription = null, tint = TerminalGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VirtIO-Net NAT Routing Matrix",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val portRules = listOf(
                        Triple("2222", "22", "SSH Remote Shell"),
                        Triple("8080", "80", "HTTP Web Server"),
                        Triple("3000", "3000", "Node.js / React Dev Server"),
                        Triple("8888", "8888", "Jupyter Python Notebook")
                    )

                    portRules.forEach { (hostPort, guestPort, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Host 127.0.0.1:$hostPort ➜ Guest :$guestPort",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = desc,
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Engine Information Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, CarbonBorder, RoundedCornerShape(10.dp)),
                colors = CardDefaults.cardColors(containerColor = CarbonSurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VirtuLinux Hypervisor Engine v9.1",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Built with Android Native KVM virtualization bridge, QEMU multi-threaded emulation, VirGL 3D Gallium drivers, and Room local persistent storage.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) TerminalGreen else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CarbonDark,
                checkedTrackColor = TerminalGreen,
                uncheckedTrackColor = CarbonBorder
            )
        )
    }
}
