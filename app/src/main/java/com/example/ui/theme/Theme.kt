package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = TerminalGreen,
  onPrimary = CarbonDark,
  primaryContainer = TerminalGreenDim.copy(alpha = 0.3f),
  onPrimaryContainer = TerminalGreen,
  secondary = NeonCyan,
  onSecondary = CarbonDark,
  secondaryContainer = NeonCyanDim.copy(alpha = 0.25f),
  onSecondaryContainer = NeonCyan,
  tertiary = ElectricBlue,
  onTertiary = Color.White,
  background = CarbonDark,
  onBackground = TextPrimary,
  surface = CarbonSurface,
  onSurface = TextPrimary,
  surfaceVariant = CarbonSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  outline = CarbonBorder,
  error = AccentRed,
  onError = Color.White
)

private val LightColorScheme = lightColorScheme(
  primary = TerminalGreenDim,
  onPrimary = Color.White,
  secondary = NeonCyanDim,
  onSecondary = Color.White,
  tertiary = ElectricBlue,
  background = LightBg,
  surface = LightSurface,
  outline = LightBorder,
  onBackground = LightText,
  onSurface = LightText,
  surfaceVariant = Color(0xFFECEFF4),
  onSurfaceVariant = Color(0xFF475569)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to dark hypervisor cockpit theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

