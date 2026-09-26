package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DiscFull
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TextSecondary

@Composable
fun HypervisorBottomNav(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .height(68.dp)
            .background(CarbonDark)
            .border(width = 0.5.dp, color = CarbonBorder),
        containerColor = CarbonDark,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple(AppScreen.DASHBOARD, "Dashboard", Icons.Default.Dashboard),
            Triple(AppScreen.VM_DISPLAY, "Desktop", Icons.Default.Monitor),
            Triple(AppScreen.VM_TERMINAL, "Console", Icons.Default.Terminal),
            Triple(AppScreen.ISO_LIBRARY, "ISOs", Icons.Default.DiscFull),
            Triple(AppScreen.STORAGE_MANAGER, "Storage", Icons.Default.FolderShared)
        )

        items.forEach { (screen, label, icon) ->
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) TerminalGreen else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        color = if (isSelected) TerminalGreen else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TerminalGreen,
                    unselectedIconColor = TextSecondary,
                    selectedTextColor = TerminalGreen,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = TerminalGreen.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
            )
        }
    }
}
