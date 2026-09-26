package com.example.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VmInstance
import com.example.data.model.VmStatus
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.ArchBadge
import com.example.ui.components.MetricPill
import com.example.ui.components.StatusBadge
import com.example.ui.create.CreateVmDialog
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

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    vms: List<VmInstance>,
    searchQuery: String,
    selectedFilter: String,
    isGridView: Boolean,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(CarbonDark)) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
            // Search and view mode switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            "Filter instances by name or distro...",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dashboard_search_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = TerminalGreen,
                        unfocusedBorderColor = CarbonBorder,
                        focusedContainerColor = CarbonSurface,
                        unfocusedContainerColor = CarbonSurface
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { viewModel.toggleViewMode() },
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(CarbonSurface)
                        .border(1.dp, CarbonBorder, RoundedCornerShape(10.dp))
                        .testTag("view_mode_toggle")
                ) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = "Toggle Grid/List",
                        tint = TerminalGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter chips
            val filterOptions = listOf(
                Pair("ALL", "All Distros"),
                Pair("RUNNING", "Running"),
                Pair("DEBIAN", "Ubuntu/Debian"),
                Pair("ARCH", "Arch"),
                Pair("ALPINE", "Alpine (Low Latency)"),
                Pair("REDHAT", "Fedora/RHEL")
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filterOptions) { (id, label) ->
                    val isSelected = selectedFilter == id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) TerminalGreen.copy(alpha = 0.2f) else CarbonSurface)
                            .border(
                                1.dp,
                                if (isSelected) TerminalGreen else CarbonBorder,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { viewModel.setDistroFilter(id) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("filter_chip_$id")
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) TerminalGreen else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Empty state or VM instances list
            if (vms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Computer,
                            contentDescription = "No VMs",
                            tint = TextSecondary,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Virtual Machines Found",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Deploy a new Linux instance or boot from the ISO library.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showCreateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = CarbonDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Create Virtual Machine",
                                color = CarbonDark,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            } else if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 310.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 76.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(vms, key = { it.id }) { vm ->
                        VmCard(
                            vm = vm,
                            onStart = { viewModel.startVm(vm) },
                            onStop = { viewModel.stopVm(vm) },
                            onPause = { viewModel.pauseVm(vm) },
                            onResume = { viewModel.resumeVm(vm) },
                            onReboot = { viewModel.rebootVm(vm) },
                            onOpenDisplay = { viewModel.navigateTo(AppScreen.VM_DISPLAY, vm.id) },
                            onOpenConsole = { viewModel.navigateTo(AppScreen.VM_TERMINAL, vm.id) },
                            onClone = { viewModel.cloneVm(vm) },
                            onDelete = { viewModel.deleteVm(vm) },
                            onSnapshot = { viewModel.takeSnapshot(vm, "Manual Snapshot") }
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 76.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(vms, key = { it.id }) { vm ->
                        VmCard(
                            vm = vm,
                            onStart = { viewModel.startVm(vm) },
                            onStop = { viewModel.stopVm(vm) },
                            onPause = { viewModel.pauseVm(vm) },
                            onResume = { viewModel.resumeVm(vm) },
                            onReboot = { viewModel.rebootVm(vm) },
                            onOpenDisplay = { viewModel.navigateTo(AppScreen.VM_DISPLAY, vm.id) },
                            onOpenConsole = { viewModel.navigateTo(AppScreen.VM_TERMINAL, vm.id) },
                            onClone = { viewModel.cloneVm(vm) },
                            onDelete = { viewModel.deleteVm(vm) },
                            onSnapshot = { viewModel.takeSnapshot(vm, "Manual Snapshot") }
                        )
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = TerminalGreen,
            contentColor = CarbonDark,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 84.dp)
                .testTag("fab_create_vm")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create VM", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "New VM",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                )
            }
        }

        if (showCreateDialog) {
            CreateVmDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { name, distroId, arch, cores, ramMb, diskGb, bootMode, gpuDriver, lowLatency, kvm, isoPath ->
                    viewModel.createVm(
                        name, distroId, arch, cores, ramMb, diskGb, bootMode, gpuDriver, lowLatency, kvm, isoPath
                    )
                }
            )
        }
    }
}

@Composable
fun VmCard(
    vm: VmInstance,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReboot: () -> Unit,
    onOpenDisplay: () -> Unit,
    onOpenConsole: () -> Unit,
    onClone: () -> Unit,
    onDelete: () -> Unit,
    onSnapshot: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val isRunning = vm.status == VmStatus.RUNNING
    val isBooting = vm.status == VmStatus.BOOTING
    val isPaused = vm.status == VmStatus.PAUSED

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (isRunning) TerminalGreen.copy(alpha = 0.5f) else CarbonBorder,
                RoundedCornerShape(12.dp)
            )
            .testTag("vm_card_${vm.id}"),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Card Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ArchBadge(vm.architecture)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = vm.distroName,
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(vm.status)

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(28.dp).testTag("vm_menu_button_${vm.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More actions",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier
                                .background(CarbonSurfaceVariant)
                                .border(1.dp, CarbonBorder)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Take Snapshot", color = TextPrimary, fontFamily = FontFamily.Monospace) },
                                onClick = { showMenu = false; onSnapshot() },
                                leadingIcon = { Icon(Icons.Default.CameraAlt, contentDescription = null, tint = NeonCyan) }
                            )
                            DropdownMenuItem(
                                text = { Text("Reboot VM", color = TextPrimary, fontFamily = FontFamily.Monospace) },
                                onClick = { showMenu = false; onReboot() },
                                leadingIcon = { Icon(Icons.Default.RestartAlt, contentDescription = null, tint = AccentAmber) }
                            )
                            DropdownMenuItem(
                                text = { Text("Clone Instance", color = TextPrimary, fontFamily = FontFamily.Monospace) },
                                onClick = { showMenu = false; onClone() },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = ElectricBlue) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete VM", color = AccentRed, fontFamily = FontFamily.Monospace) },
                                onClick = { showMenu = false; onDelete() },
                                leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = AccentRed) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // VM Name
            Text(
                text = vm.name,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Hardware Specs Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MetricPill(label = "CPU", value = "${vm.vCpuCores}c")
                MetricPill(label = "RAM", value = "${vm.ramMb}M")
                MetricPill(label = "Disk", value = "${vm.diskSizeGb}G (${vm.diskFormat})")
                MetricPill(label = "GPU", value = vm.gpuDriver.replace("_", " "))
            }

            // Real-time telemetry if active
            AnimatedVisibility(visible = isRunning || isBooting) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(CarbonDark)
                            .border(0.5.dp, TerminalGreen.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = TerminalGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "CPU: ${vm.cpuUsagePercent.toInt()}%",
                                color = TerminalGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "RAM: ${vm.ramUsageMb}MB",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "Up: ${vm.uptimeSeconds / 60}m ${vm.uptimeSeconds % 60}s",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when {
                    isRunning -> {
                        Button(
                            onClick = onOpenDisplay,
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("action_display_${vm.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Monitor,
                                contentDescription = null,
                                tint = CarbonDark,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Display",
                                color = CarbonDark,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }

                        OutlinedButton(
                            onClick = onOpenConsole,
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(NeonCyan)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("action_console_${vm.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "TTY",
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }

                        IconButton(
                            onClick = onStop,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentRed.copy(alpha = 0.15f))
                                .border(1.dp, AccentRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .size(38.dp)
                                .testTag("action_stop_${vm.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = "Power Off",
                                tint = AccentRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    isPaused -> {
                        Button(
                            onClick = onResume,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resume", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onStop,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kill", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        }
                    }

                    isBooting -> {
                        Button(
                            onClick = onOpenDisplay,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Booting Kernel...",
                                color = CarbonDark,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                    }

                    else -> {
                        Button(
                            onClick = onStart,
                            colors = ButtonDefaults.buttonColors(containerColor = CarbonSurfaceVariant),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(TerminalGreen)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("action_start_${vm.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = TerminalGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Start VM",
                                color = TerminalGreen,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }

                        OutlinedButton(
                            onClick = onOpenConsole,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Config",
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
