package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Cyberpunk OLED Base
val OledBlack = Color(0xFF000000)
val CyberBackground = Color(0xFF05080F)
val CyberSurface = Color(0xFF0D121F)
val CyberCard = Color(0xFF131A2B)
val CyberCardBorder = Color(0xFF1E283D)
val CyberTextPrimary = Color(0xFFF0F6FC)
val CyberTextSecondary = Color(0xFF8B949E)
val CyberTextMuted = Color(0xFF484F58)

// Neon Accents
val NeonCyan = Color(0xFF00F0FF)
val NeonCyanDark = Color(0xFF009AA3)
val MatrixGreen = Color(0xFF00FF66)
val MatrixGreenDark = Color(0xFF00A340)
val LaserViolet = Color(0xFFBD00FF)
val LaserVioletDark = Color(0xFF7A00A3)
val SolarAmber = Color(0xFFFFB800)
val SolarAmberDark = Color(0xFFA37500)
val NeonCrimson = Color(0xFFFF0055)

// Security Threat Badges
val ThreatSafe = Color(0xFF00FF88)
val ThreatSafeBg = Color(0xFF0A2B18)
val ThreatWarning = Color(0xFFFFB800)
val ThreatWarningBg = Color(0xFF332500)
val ThreatDanger = Color(0xFFFF2255)
val ThreatDangerBg = Color(0xFF3B0011)

enum class CyberThemePalette(
    val title: String,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color
) {
    CYBER_CYAN("Cyber Cyan", NeonCyan, MatrixGreen, LaserViolet),
    MATRIX_GREEN("Matrix Green", MatrixGreen, NeonCyan, SolarAmber),
    LASER_VIOLET("Laser Violet", LaserViolet, NeonCyan, NeonCrimson),
    SOLAR_AMBER("Solar Amber", SolarAmber, NeonCrimson, NeonCyan)
}
