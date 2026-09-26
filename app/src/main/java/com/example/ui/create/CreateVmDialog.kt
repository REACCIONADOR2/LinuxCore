package com.example.ui.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.BootMode
import com.example.data.model.DistroCatalog
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CreateVmDialog(
    onDismiss: () -> Unit,
    onCreate: (
        name: String,
        distroId: String,
        arch: String,
        cores: Int,
        ramMb: Int,
        diskGb: Int,
        bootMode: BootMode,
        gpuDriver: String,
        lowLatency: Boolean,
        kvm: Boolean,
        isoPath: String?
    ) -> Unit
) {
    val templates = DistroCatalog.templates
    var selectedTemplate by remember { mutableStateOf(templates.first()) }
    var vmName by remember { mutableStateOf("${selectedTemplate.name} Instance") }
    var selectedArch by remember { mutableStateOf(selectedTemplate.architecture) }
    var vCpuCores by remember { mutableFloatStateOf(2f) }
    var ramMb by remember { mutableFloatStateOf(selectedTemplate.recommendedRamMb.toFloat()) }
    var diskGb by remember { mutableFloatStateOf(selectedTemplate.defaultDiskGb.toFloat()) }
    var bootFromIso by remember { mutableStateOf(false) }
    var selectedGpuDriver by remember { mutableStateOf("VIRGL_3D") }
    var lowLatencyPreempt by remember { mutableStateOf(true) }
    var kvmAccelerated by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
                .testTag("create_vm_dialog"),
            colors = CardDefaults.cardColors(containerColor = CarbonSurface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "New Virtual Machine",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Configure kernel & hardware emulation",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // VM Name
                Text(
                    text = "INSTANCE NAME",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = vmName,
                    onValueChange = { vmName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vm_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = TerminalGreen,
                        unfocusedBorderColor = CarbonBorder,
                        focusedContainerColor = CarbonDark,
                        unfocusedContainerColor = CarbonDark
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Select Distro
                Text(
                    text = "SELECT LINUX DISTRO",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(templates) { template ->
                        val isSelected = template.id == selectedTemplate.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) TerminalGreen.copy(alpha = 0.2f) else CarbonDark)
                                .border(
                                    1.dp,
                                    if (isSelected) TerminalGreen else CarbonBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedTemplate = template
                                    vmName = "${template.name} Instance"
                                    selectedArch = template.architecture
                                    ramMb = template.recommendedRamMb.toFloat()
                                    diskGb = template.defaultDiskGb.toFloat()
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Text(
                                    text = template.name,
                                    color = if (isSelected) TerminalGreen else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${template.family} • ${template.architecture}",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Architecture toggle
                Text(
                    text = "CPU ARCHITECTURE",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("x86_64", "aarch64", "riscv64").forEach { arch ->
                        val isSelected = selectedArch == arch
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else CarbonDark)
                                .border(1.dp, if (isSelected) NeonCyan else CarbonBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedArch = arch }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = arch,
                                color = if (isSelected) NeonCyan else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // vCPU Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "VIRTUAL CPU CORES",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${vCpuCores.toInt()} Cores",
                        color = TerminalGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Slider(
                    value = vCpuCores,
                    onValueChange = { vCpuCores = it },
                    valueRange = 1f..8f,
                    steps = 6,
                    colors = SliderDefaults.colors(
                        thumbColor = TerminalGreen,
                        activeTrackColor = TerminalGreen,
                        inactiveTrackColor = CarbonBorder
                    )
                )

                // RAM Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "MEMORY ALLOCATION",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${ramMb.toInt()} MB",
                        color = TerminalGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Slider(
                    value = ramMb,
                    onValueChange = { ramMb = it },
                    valueRange = 512f..8192f,
                    steps = 14,
                    colors = SliderDefaults.colors(
                        thumbColor = TerminalGreen,
                        activeTrackColor = TerminalGreen,
                        inactiveTrackColor = CarbonBorder
                    )
                )

                // Virtual Disk Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "VIRTUAL STORAGE (QCOW2)",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${diskGb.toInt()} GB",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Slider(
                    value = diskGb,
                    onValueChange = { diskGb = it },
                    valueRange = 8f..64f,
                    steps = 6,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = CarbonBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Hardware Acceleration & Driver Switches
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "VirGL 3D GPU Acceleration",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Direct OpenGL ES 3.2 host rendering (60 FPS)",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Switch(
                        checked = selectedGpuDriver == "VIRGL_3D",
                        onCheckedChange = { selectedGpuDriver = if (it) "VIRGL_3D" else "VIRTIO_GPU" },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CarbonDark,
                            checkedTrackColor = TerminalGreen,
                            uncheckedTrackColor = CarbonBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PREEMPT_RT Low Latency Kernel",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "1000Hz real-time scheduling (<3ms latency)",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Switch(
                        checked = lowLatencyPreempt,
                        onCheckedChange = { lowLatencyPreempt = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CarbonDark,
                            checkedTrackColor = TerminalGreen,
                            uncheckedTrackColor = CarbonBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Boot Mode: Live .ISO",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (bootFromIso) "Boot directly from ${selectedTemplate.isoFileName}" else "Boot from persistent virtual disk",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Switch(
                        checked = bootFromIso,
                        onCheckedChange = { bootFromIso = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CarbonDark,
                            checkedTrackColor = NeonCyan,
                            uncheckedTrackColor = CarbonBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = CarbonDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel", color = TextSecondary, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            onCreate(
                                vmName,
                                selectedTemplate.id,
                                selectedArch,
                                vCpuCores.toInt(),
                                ramMb.toInt(),
                                diskGb.toInt(),
                                if (bootFromIso) BootMode.ISO_BOOT else BootMode.VIRTUAL_DISK,
                                selectedGpuDriver,
                                lowLatencyPreempt,
                                kvmAccelerated,
                                if (bootFromIso) "virtu://images/${selectedTemplate.isoFileName}" else null
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("submit_create_vm")
                    ) {
                        Text(
                            "Deploy Virtual Machine",
                            color = CarbonDark,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
