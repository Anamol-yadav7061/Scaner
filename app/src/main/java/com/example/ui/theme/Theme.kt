package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun AnamolQRTheme(
    palette: CyberThemePalette = CyberThemePalette.CYBER_CYAN,
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = palette.primary,
        onPrimary = OledBlack,
        primaryContainer = palette.primary.copy(alpha = 0.2f),
        onPrimaryContainer = palette.primary,
        secondary = palette.secondary,
        onSecondary = OledBlack,
        secondaryContainer = palette.secondary.copy(alpha = 0.2f),
        onSecondaryContainer = palette.secondary,
        tertiary = palette.tertiary,
        onTertiary = OledBlack,
        background = OledBlack,
        onBackground = CyberTextPrimary,
        surface = CyberSurface,
        onSurface = CyberTextPrimary,
        surfaceVariant = CyberCard,
        onSurfaceVariant = CyberTextSecondary,
        outline = palette.primary.copy(alpha = 0.35f),
        outlineVariant = CyberCardBorder,
        error = NeonCrimson,
        onError = Color.White
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
