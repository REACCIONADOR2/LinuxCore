package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.HypervisorBottomNav
import com.example.ui.components.HypervisorHeader
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.display.VmDisplayScreen
import com.example.ui.iso.IsoLibraryScreen
import com.example.ui.settings.HypervisorSettingsScreen
import com.example.ui.storage.StorageManagerScreen
import com.example.ui.terminal.VmTerminalScreen
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                VirtuLinuxApp()
            }
        }
    }
}

@Composable
fun VirtuLinuxApp(
    viewModel: MainViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val hostStats by viewModel.hostStats.collectAsState()
    val filteredVms by viewModel.filteredVms.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedDistroFilter.collectAsState()
    val isGridView by viewModel.isGridView.collectAsState()
    val activeEditingFile by viewModel.activeEditingFile.collectAsState()

    // BackHandler support for sub-screens
    BackHandler(enabled = currentScreen != AppScreen.DASHBOARD || activeEditingFile != null) {
        if (activeEditingFile != null) {
            viewModel.closeEditingFile()
        } else {
            viewModel.navigateTo(AppScreen.DASHBOARD)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CarbonDark,
        topBar = {
            if (currentScreen != AppScreen.HYPERVISOR_SETTINGS) {
                HypervisorHeader(
                    hostStats = hostStats,
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        },
        bottomBar = {
            HypervisorBottomNav(
                currentScreen = currentScreen,
                onNavigate = { viewModel.navigateTo(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CarbonDark)
        ) {
            when (currentScreen) {
                AppScreen.DASHBOARD -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        vms = filteredVms,
                        searchQuery = searchQuery,
                        selectedFilter = selectedFilter,
                        isGridView = isGridView
                    )
                }

                AppScreen.VM_DISPLAY -> {
                    VmDisplayScreen(
                        viewModel = viewModel
                    )
                }

                AppScreen.VM_TERMINAL -> {
                    VmTerminalScreen(
                        viewModel = viewModel
                    )
                }

                AppScreen.ISO_LIBRARY -> {
                    IsoLibraryScreen(
                        viewModel = viewModel
                    )
                }

                AppScreen.STORAGE_MANAGER -> {
                    StorageManagerScreen(
                        viewModel = viewModel
                    )
                }

                AppScreen.HYPERVISOR_SETTINGS -> {
                    HypervisorSettingsScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                    )
                }
            }
        }
    }
}
