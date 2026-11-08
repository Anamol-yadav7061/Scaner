package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AnamolApplication
import com.example.R
import com.example.assistant.FloatingOverlayService
import com.example.ui.components.RotatingPiIndicator
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberThemePalette
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.MonospaceFont
import com.example.ui.theme.NeonCyan

@Composable
fun SettingsScreen(
    currentPalette: CyberThemePalette,
    onPaletteChange: (CyberThemePalette) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as? AnamolApplication

    var isSoundOn by remember { mutableStateOf(app?.isSoundEnabled() ?: true) }
    var currentHaptic by remember { mutableStateOf(app?.getHapticMode() ?: "TACTICAL") }
    var selectedLanguage by remember { mutableStateOf(app?.getSelectedLanguage() ?: "en") }
    var isOverlayRunning by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) {
        // Top Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp, start = 20.dp, end = 20.dp, bottom = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(NeonCyan, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.settings_title),
                    color = NeonCyan,
                    fontFamily = MonospaceFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Text(
                text = "SECURITY ENGINE & CUSTOMIZATION HUB",
                color = CyberTextSecondary,
                fontFamily = MonospaceFont,
                fontSize = 11.sp
            )
        }

        // Scrollable Settings
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // 1. Language Localization Hub
            SettingsSectionHeader(icon = Icons.Default.Translate, title = stringResource(R.string.language))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    val languages = listOf(
                        Triple("en", "English", "Default US/Global"),
                        Triple("hi", "हिन्दी (Hindi)", "पूर्ण भारतीय स्थानीयकरण"),
                        Triple("ur", "اردو (Urdu)", "مکمل اردو لوکلائزیشن")
                    )

                    languages.forEach { (code, name, sub) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedLanguage = code
                                    app?.setSelectedLanguage(code)
                                    Toast.makeText(context, "Language switched to $name", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = name,
                                    color = if (selectedLanguage == code) NeonCyan else Color.White,
                                    fontFamily = MonospaceFont,
                                    fontWeight = if (selectedLanguage == code) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                                Text(text = sub, color = CyberTextSecondary, fontSize = 11.sp)
                            }
                            if (selectedLanguage == code) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Assistant & Screen Scanner Overlay
            SettingsSectionHeader(icon = Icons.Default.Assistant, title = stringResource(R.string.section_assistant))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = stringResource(R.string.circle_to_search_hint),
                        color = CyberTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_VOICE_INPUT_SETTINGS)
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                try {
                                    val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Navigate to Phone Settings -> Default Apps -> Assist", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "SET AS DEFAULT ASSISTANT",
                            color = Color.Black,
                            fontFamily = MonospaceFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Floating Screen Scanner Button",
                                color = Color.White,
                                fontFamily = MonospaceFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Overlay HUD badge on phone screen",
                                color = CyberTextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Switch(
                            checked = isOverlayRunning,
                            onCheckedChange = { enable ->
                                if (enable) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        context.startActivity(intent)
                                        Toast.makeText(context, "Grant 'Display over other apps' permission", Toast.LENGTH_LONG).show()
                                    } else {
                                        context.startService(Intent(context, FloatingOverlayService::class.java))
                                        isOverlayRunning = true
                                        Toast.makeText(context, "Floating badge activated", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    context.stopService(Intent(context, FloatingOverlayService::class.java))
                                    isOverlayRunning = false
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = NeonCyan
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Haptics & Audio
            SettingsSectionHeader(icon = Icons.Default.Vibration, title = stringResource(R.string.section_haptics))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.audio_chirp),
                                color = Color.White,
                                fontFamily = MonospaceFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(text = "Hacker tone synthesizer on decode", color = CyberTextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = isSoundOn,
                            onCheckedChange = {
                                isSoundOn = it
                                app?.setSoundEnabled(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = MatrixGreen
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "HAPTIC VIBRATION INTENSITY:",
                        color = NeonCyan,
                        fontFamily = MonospaceFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("OFF", "SUBTLE", "TACTICAL", "CYBER").forEach { mode ->
                            FilterChip(
                                selected = currentHaptic == mode,
                                onClick = {
                                    currentHaptic = mode
                                    app?.setHapticMode(mode)
                                    app?.playHapticFeedback()
                                },
                                label = { Text(mode, fontFamily = MonospaceFont, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonCyan,
                                    selectedLabelColor = Color.Black
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Cyber Theme Palette Switcher
            SettingsSectionHeader(icon = Icons.Default.Palette, title = stringResource(R.string.section_theme))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CyberThemePalette.entries.forEach { p ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = if (currentPalette == p) 2.dp else 1.dp,
                                color = if (currentPalette == p) p.primary else CyberCardBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onPaletteChange(p) },
                        colors = CardDefaults.cardColors(containerColor = CyberCard),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .background(p.primary, CircleShape)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = p.title.substringBefore(" "),
                                color = if (currentPalette == p) p.primary else CyberTextSecondary,
                                fontFamily = MonospaceFont,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Rotating Pi & About App Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RotatingPiIndicator(size = 56.dp, color = NeonCyan)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "ANAMOL QR CODE SCANNER",
                            color = NeonCyan,
                            fontFamily = MonospaceFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Security Tooling & Optical Analyzer",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "ML Kit Engine • Encrypted Room DB • Pi HUD",
                            color = CyberTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = MonospaceFont
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        modifier = Modifier.padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title.uppercase(),
            color = NeonCyan,
            fontFamily = MonospaceFont,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}
