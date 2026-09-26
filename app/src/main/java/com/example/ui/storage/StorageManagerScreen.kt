package com.example.ui.storage

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.VirtualFile
import com.example.ui.MainViewModel
import com.example.ui.components.MetricPill
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
fun StorageManagerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val vms by viewModel.allVms.collectAsState()
    val hostStats by viewModel.hostStats.collectAsState()
    val currentStoragePath by viewModel.currentStoragePath.collectAsState()
    val activeEditingFile by viewModel.activeEditingFile.collectAsState()

    var showNewFileDialog by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CarbonDark)
            .testTag("storage_manager_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CarbonSurface)
                .border(0.5.dp, CarbonBorder)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Persistent Storage & File Manager",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "QCOW2 Sparse Disks • ext4 / btrfs • VirtIO-9P Host Share",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(24.dp)
            )
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CarbonDark,
            contentColor = TerminalGreen,
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = TerminalGreen
                    )
                }
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        "File System Explorer",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        "Virtual Disks & Pools",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            )
        }

        if (activeEditingFile != null) {
            // Built-in File Editor Mode
            FileEditorView(
                file = activeEditingFile!!,
                onSave = { content -> viewModel.saveActiveFile(content) },
                onClose = { viewModel.closeEditingFile() }
            )
        } else if (selectedTab == 0) {
            // File System Explorer Tab
            FileSystemExplorerTab(
                viewModel = viewModel,
                currentPath = currentStoragePath,
                onOpenNewFile = { showNewFileDialog = true },
                onOpenNewFolder = { showNewFolderDialog = true }
            )
        } else {
            // Virtual Disks Tab
            VirtualDisksTab(
                vms = vms,
                hostStats = hostStats
            )
        }

        if (showNewFileDialog) {
            NewItemDialog(
                title = "Create New File",
                label = "File Name (e.g. script.py, config.conf)",
                onDismiss = { showNewFileDialog = false },
                onConfirm = { name ->
                    viewModel.createNewFileInStorage(name, "#!/bin/bash\n# Created in VirtuLinux Storage Manager\n")
                    showNewFileDialog = false
                }
            )
        }

        if (showNewFolderDialog) {
            NewItemDialog(
                title = "Create New Folder",
                label = "Directory Name",
                onDismiss = { showNewFolderDialog = false },
                onConfirm = { name ->
                    viewModel.createNewFolderInStorage(name)
                    showNewFolderDialog = false
                }
            )
        }
    }
}

@Composable
fun FileSystemExplorerTab(
    viewModel: MainViewModel,
    currentPath: String,
    onOpenNewFile: () -> Unit,
    onOpenNewFolder: () -> Unit
) {
    val engine = viewModel.engine
    val files = remember(currentPath) { engine.fileSystem.listFiles(currentPath) }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        // Breadcrumb & Action bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CarbonSurface)
                .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (currentPath != "/") {
                    IconButton(
                        onClick = {
                            val lastSlash = currentPath.lastIndexOf('/')
                            val parent = if (lastSlash <= 0) "/" else currentPath.substring(0, lastSlash)
                            viewModel.setStoragePath(parent)
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Up directory",
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = currentPath,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row {
                IconButton(onClick = onOpenNewFile, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "New File", tint = TerminalGreen, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onOpenNewFolder, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder", tint = NeonCyan, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Shared host folder indicator banner
        if (currentPath.contains("shared_android")) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(NeonCyan.copy(alpha = 0.15f))
                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FolderShared, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Host ↔ Guest Shared Folder (VirtIO-9P Zero-Copy Sync)",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // File List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(files) { file ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(0.5.dp, CarbonBorder, RoundedCornerShape(8.dp))
                        .clickable {
                            if (file.isDirectory) {
                                viewModel.setStoragePath(file.path)
                            } else {
                                viewModel.openFileForEditing(file)
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = CarbonSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = if (file.isDirectory) {
                                    if (file.isHostShared) Icons.Default.FolderShared else Icons.Default.Folder
                                } else {
                                    Icons.Default.Description
                                },
                                contentDescription = null,
                                tint = if (file.isDirectory) NeonCyan else TerminalGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = file.name,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${file.permissions} • ${file.owner}:${file.group}",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = file.displaySize,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { viewModel.deleteFileFromStorage(file.path) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRed, modifier = Modifier.size(15.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FileEditorView(
    file: VirtualFile,
    onSave: (String) -> Unit,
    onClose: () -> Unit
) {
    var content by remember(file.path) { mutableStateOf(file.content) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Editor Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CarbonSurface)
                .border(1.dp, CarbonBorder)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Code, contentDescription = null, tint = TerminalGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${file.path} ${if (hasUnsavedChanges) "*" else ""}",
                    color = if (hasUnsavedChanges) TerminalGreen else TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row {
                Button(
                    onClick = {
                        onSave(content)
                        hasUnsavedChanges = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = CarbonDark, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save", color = CarbonDark, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = onClose, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Editor TextArea
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CarbonSurfaceVariant)
                .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            BasicTextField(
                value = content,
                onValueChange = {
                    content = it
                    hasUnsavedChanges = true
                },
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp
                ),
                cursorBrush = SolidColor(TerminalGreen),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun VirtualDisksTab(
    vms: List<com.example.data.model.VmInstance>,
    hostStats: com.example.data.model.HypervisorHostStats
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Storage Pool Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = CarbonSurface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Hypervisor Storage Pool (VirtIO-BLK)",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sparse QCOW2 images with copy-on-write snapshots and zstd transparent compression.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Pool Used Space", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("${hostStats.usedStorageGb} GB / ${hostStats.totalStorageGb} GB", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { (hostStats.usedStorageGb.toFloat() / hostStats.totalStorageGb.toFloat()).coerceIn(0.1f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = NeonCyan,
                    trackColor = CarbonSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "ATTACHED VIRTUAL DISKS",
            color = NeonCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(vms) { vm ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(0.5.dp, CarbonBorder, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = CarbonSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${vm.name}.qcow2",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Target: /dev/vda • Format: ${vm.diskFormat} • Cache: writeback",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${vm.diskSizeGb} GB Max",
                                color = TerminalGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Allocated: ${(vm.diskSizeGb * 0.28).toInt()} GB",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewItemDialog(
    title: String,
    label: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = CarbonSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(label, color = TextSecondary, fontFamily = FontFamily.Monospace) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = TerminalGreen,
                        unfocusedBorderColor = CarbonBorder
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = CarbonDark)
                    ) {
                        Text("Cancel", color = TextSecondary, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) onConfirm(name.trim())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen)
                    ) {
                        Text("Create", color = CarbonDark, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
