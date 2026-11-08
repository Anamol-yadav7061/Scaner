package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.R
import com.example.generator.QrEccLevel
import com.example.generator.QrMatrixGenerator
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.LaserViolet
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.MonospaceFont
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.OledBlack
import com.example.ui.theme.SolarAmber

enum class GeneratorType(val label: String) {
    TEXT("Text"),
    URL("URL"),
    WIFI("WiFi"),
    UPI("UPI"),
    VCARD("Contact")
}

@Composable
fun GeneratorScreen() {
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf(GeneratorType.TEXT) }

    // Form inputs
    var textInput by remember { mutableStateOf("https://github.com/anamol-qr") }
    var urlInput by remember { mutableStateOf("https://example.com") }
    var wifiSsid by remember { mutableStateOf("Cyber_Matrix_5G") }
    var wifiPassword by remember { mutableStateOf("neoncipher2026") }
    var wifiAuth by remember { mutableStateOf("WPA") }
    var upiVpa by remember { mutableStateOf("anamol@upi") }
    var upiName by remember { mutableStateOf("Anamol Systems") }
    var upiAmount by remember { mutableStateOf("100.00") }
    var upiNote by remember { mutableStateOf("Security tooling audit") }
    var vcardName by remember { mutableStateOf("Alex Cyber") }
    var vcardPhone by remember { mutableStateOf("+1 555 0199") }
    var vcardEmail by remember { mutableStateOf("agent@cybersec.org") }

    // Customization parameters
    var selectedEcc by remember { mutableStateOf(QrEccLevel.HIGH) }
    var foregroundColor by remember { mutableStateOf(NeonCyan) }
    var backgroundColor by remember { mutableStateOf(OledBlack) }

    val rawPayload by remember {
        derivedStateOf {
            when (selectedType) {
                GeneratorType.TEXT -> textInput
                GeneratorType.URL -> if (urlInput.startsWith("http://", true) || urlInput.startsWith("https://", true)) urlInput else "https://$urlInput"
                GeneratorType.WIFI -> "WIFI:S:$wifiSsid;T:$wifiAuth;P:$wifiPassword;H:false;;"
                GeneratorType.UPI -> "upi://pay?pa=$upiVpa&pn=${Uri.encode(upiName)}&am=$upiAmount&cu=INR&tn=${Uri.encode(upiNote)}"
                GeneratorType.VCARD -> "BEGIN:VCARD\nVERSION:3.0\nFN:$vcardName\nTEL:$vcardPhone\nEMAIL:$vcardEmail\nEND:VCARD"
            }
        }
    }

    val generatedBitmap: Bitmap by remember(rawPayload, selectedEcc, foregroundColor, backgroundColor) {
        derivedStateOf {
            QrMatrixGenerator.generateQrBitmap(
                content = rawPayload,
                eccLevel = selectedEcc,
                foregroundColor = foregroundColor.toArgb(),
                backgroundColor = backgroundColor.toArgb(),
                size = 700
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) {
        // Top App Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp, start = 20.dp, end = 20.dp, bottom = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(NeonCyan, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.gen_title),
                    color = NeonCyan,
                    fontFamily = MonospaceFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Text(
                text = "SYNTHESIZE CUSTOM HIGH-DENSITY QR MATRIX",
                color = CyberTextSecondary,
                fontFamily = MonospaceFont,
                fontSize = 11.sp
            )
        }

        // Scrollable Tabs for Generator Types
        ScrollableTabRow(
            selectedTabIndex = selectedType.ordinal,
            containerColor = CyberCard,
            contentColor = NeonCyan,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedType.ordinal]),
                    color = NeonCyan
                )
            }
        ) {
            GeneratorType.entries.forEach { type ->
                Tab(
                    selected = selectedType == type,
                    onClick = { selectedType = type },
                    text = {
                        Text(
                            text = type.label.uppercase(),
                            fontFamily = MonospaceFont,
                            fontWeight = if (selectedType == type) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (selectedType == type) NeonCyan else CyberTextSecondary
                        )
                    }
                )
            }
        }

        // Scrollable Body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Live Synthesized QR Preview Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, foregroundColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .background(backgroundColor, RoundedCornerShape(12.dp))
                            .border(1.dp, foregroundColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = generatedBitmap.asImageBitmap(),
                            contentDescription = "Synthesized QR Matrix",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "ECC: ${selectedEcc.label} • ${rawPayload.toByteArray().size} BYTES",
                        color = MatrixGreen,
                        fontFamily = MonospaceFont,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Export & Share Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    val file = QrMatrixGenerator.saveBitmapToCache(context, generatedBitmap)
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/png"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share QR Matrix"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Share error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = foregroundColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share QR", color = Color.Black, fontFamily = MonospaceFont, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                try {
                                    val file = QrMatrixGenerator.saveBitmapToCache(context, generatedBitmap, "anamol_qr_${System.currentTimeMillis()}.png")
                                    Toast.makeText(context, "Saved high-res PNG to cache: ${file.name}", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Save error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export PNG", color = Color.White, fontFamily = MonospaceFont, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dynamic Input Fields based on selected tab
            Text(
                text = "PAYLOAD CONFIGURATION",
                color = NeonCyan,
                fontFamily = MonospaceFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            when (selectedType) {
                GeneratorType.TEXT -> {
                    CyberOutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        label = "Raw Text Content"
                    )
                }
                GeneratorType.URL -> {
                    CyberOutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = "Website Destination (https://...)"
                    )
                }
                GeneratorType.WIFI -> {
                    CyberOutlinedTextField(
                        value = wifiSsid,
                        onValueChange = { wifiSsid = it },
                        label = "WiFi Network Name (SSID)"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CyberOutlinedTextField(
                        value = wifiPassword,
                        onValueChange = { wifiPassword = it },
                        label = "Network Security Password"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("WPA", "WEP", "NONE").forEach { auth ->
                            FilterChip(
                                selected = wifiAuth == auth,
                                onClick = { wifiAuth = auth },
                                label = { Text(auth, fontFamily = MonospaceFont, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonCyan,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                }
                GeneratorType.UPI -> {
                    CyberOutlinedTextField(
                        value = upiVpa,
                        onValueChange = { upiVpa = it },
                        label = "Payee VPA (e.g. merchant@okhdfcbank)"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CyberOutlinedTextField(
                        value = upiName,
                        onValueChange = { upiName = it },
                        label = "Payee Display Name"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CyberOutlinedTextField(
                        value = upiAmount,
                        onValueChange = { upiAmount = it },
                        label = "Amount in INR (₹)"
                    )
                }
                GeneratorType.VCARD -> {
                    CyberOutlinedTextField(
                        value = vcardName,
                        onValueChange = { vcardName = it },
                        label = "Full Name"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CyberOutlinedTextField(
                        value = vcardPhone,
                        onValueChange = { vcardPhone = it },
                        label = "Phone Number"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CyberOutlinedTextField(
                        value = vcardEmail,
                        onValueChange = { vcardEmail = it },
                        label = "Email Address"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Error Correction Level Selector
            Text(
                text = "ERROR CORRECTION CODE (ECC)",
                color = NeonCyan,
                fontFamily = MonospaceFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QrEccLevel.entries.forEach { ecc ->
                    FilterChip(
                        selected = selectedEcc == ecc,
                        onClick = { selectedEcc = ecc },
                        label = { Text(ecc.label, fontFamily = MonospaceFont, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MatrixGreen,
                            selectedLabelColor = Color.Black
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Neon Color Pickers
            Text(
                text = "NEON COLOR MATRIX",
                color = NeonCyan,
                fontFamily = MonospaceFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text("Foreground Pixel Color:", color = CyberTextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(NeonCyan, MatrixGreen, LaserViolet, SolarAmber, NeonCrimson, Color.White).forEach { c ->
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(c, CircleShape)
                            .border(
                                width = if (foregroundColor == c) 3.dp else 1.dp,
                                color = if (foregroundColor == c) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { foregroundColor = c }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Background Surface Color:", color = CyberTextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(OledBlack, CyberBackground, Color(0xFF0D1B2A), Color.White).forEach { c ->
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(c, CircleShape)
                            .border(
                                width = if (backgroundColor == c) 3.dp else 1.dp,
                                color = if (backgroundColor == c) NeonCyan else CyberCardBorder,
                                shape = CircleShape
                            )
                            .clickable { backgroundColor = c }
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun CyberOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontFamily = MonospaceFont, fontSize = 12.sp) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = CyberCardBorder,
            focusedTextColor = CyberTextPrimary,
            unfocusedTextColor = CyberTextPrimary,
            focusedContainerColor = CyberCard,
            unfocusedContainerColor = CyberCard
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    )
}
