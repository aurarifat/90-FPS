package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BoosterColorScheme = darkColorScheme(
    primary = NeonYellow,
    onPrimary = Color(0xFF1A1700),
    primaryContainer = YellowContainer,
    onPrimaryContainer = OnYellowContainer,
    secondary = CyberCyan,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF004F58),
    onSecondaryContainer = Color(0xFF97F0FF),
    tertiary = WarningOrange,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = TextWhite,
    surface = DarkSurface,
    onSurface = TextWhite,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextGray,
    outline = CardBorder,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun FpsBoosterTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BoosterColorScheme,
        typography = Typography,
        content = content
    )
}
