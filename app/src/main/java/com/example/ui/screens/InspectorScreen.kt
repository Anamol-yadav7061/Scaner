package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.analyzer.HackerDataInspector
import com.example.analyzer.ParsedPayload
import com.example.data.model.ScanItem
import com.example.ui.components.HackerHexView
import com.example.ui.components.SecurityBadge
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.MonospaceFont
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SolarAmber
import kotlinx.coroutines.launch

@Composable
fun InspectorScreen(
    rawContent: String,
    format: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val app = context.applicationContext as? AnamolApplication

    val report = remember(rawContent, format) {
        HackerDataInspector.inspect(rawContent, format)
    }

    var isBookmarked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp, start = 16.dp, end = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(CyberCard, CircleShape)
                        .border(1.dp, CyberCardBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NeonCyan
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.inspector_title),
                        color = NeonCyan,
                        fontFamily = MonospaceFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "FORMAT: ${report.format} • ${report.byteCount} BYTES",
                        color = CyberTextSecondary,
                        fontFamily = MonospaceFont,
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(
                onClick = {
                    isBookmarked = !isBookmarked
                    coroutineScope.launch {
                        app?.repository?.recordScan(
                            rawContent = report.raw,
                            format = report.format,
                            parsedType = report.mimeType,
                            securityRating = report.threatLevel.name,
                            title = report.raw.take(30)
                        )
                    }
                    Toast.makeText(context, if (isBookmarked) "Saved to Logs" else "Removed", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(CyberCard, CircleShape)
                    .border(1.dp, CyberCardBorder, CircleShape)
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (isBookmarked) SolarAmber else Color.White
                )
            }
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Security Threat Rating Card
            SecurityBadge(
                threatLevel = report.threatLevel,
                reasons = report.securityReasons
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Specialized Parsed Payload Card (WiFi, UPI, vCard, URL, etc.)
            SmartPayloadCard(report = report)

            Spacer(modifier = Modifier.height(16.dp))

            // Raw String Payload Card
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
                        Text(
                            text = stringResource(R.string.raw_payload),
                            color = NeonCyan,
                            fontFamily = MonospaceFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Row {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Payload", report.raw))
                                    Toast.makeText(context, "Payload Copied", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = NeonCyan, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = {
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        putExtra(Intent.EXTRA_TEXT, report.raw)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Payload"))
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = NeonCyan, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF070B12), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF141F30), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = report.raw,
                            color = CyberTextPrimary,
                            fontFamily = MonospaceFont,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hex Dump Viewer
            HackerHexView(
                hexDump = report.hexDump,
                byteCount = report.byteCount,
                charSet = report.charSet
            )

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun SmartPayloadCard(report: com.example.analyzer.InspectionReport) {
    val context = LocalContext.current

    when (val p = report.payload) {
        is ParsedPayload.Url -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "WEB TARGET • ${p.scheme.uppercase()}",
                        color = NeonCyan,
                        fontFamily = MonospaceFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = p.host,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (p.path.isNotBlank() && p.path != "/") {
                        Text(
                            text = p.path,
                            color = CyberTextSecondary,
                            fontFamily = MonospaceFont,
                            fontSize = 12.sp
                        )
                    }

                    if (p.queryParams.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "QUERY PARAMETERS (${p.queryParams.size}):",
                            color = MatrixGreen,
                            fontFamily = MonospaceFont,
                            fontSize = 11.sp
                        )
                        p.queryParams.forEach { (k, v) ->
                            Text(
                                text = "  $k = $v",
                                color = CyberTextPrimary,
                                fontFamily = MonospaceFont,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(p.url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open URL: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "OPEN TARGET LINK", color = Color.Black, fontFamily = MonospaceFont, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        is ParsedPayload.WiFi -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MatrixGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Wifi, contentDescription = null, tint = MatrixGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "WIFI NETWORK CONFIGURATION", color = MatrixGreen, fontFamily = MonospaceFont, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "SSID: ${p.ssid}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = "Password: ${p.password.ifBlank { "(None / Open)" }}", color = CyberTextPrimary, fontFamily = MonospaceFont, fontSize = 13.sp)
                    Text(text = "Security: ${p.authType} • Hidden: ${p.isHidden}", color = CyberTextSecondary, fontFamily = MonospaceFont, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("WiFi Password", p.password))
                                Toast.makeText(context, "WiFi Password Copied", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MatrixGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Copy Password", color = Color.Black, fontFamily = MonospaceFont, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        is ParsedPayload.Upi -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF00FF88), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payment, contentDescription = null, tint = Color(0xFF00FF88))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "UPI DIGITAL PAYMENT", color = Color(0xFF00FF88), fontFamily = MonospaceFont, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Payee: ${p.payeeName}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = "VPA: ${p.vpa}", color = CyberTextPrimary, fontFamily = MonospaceFont, fontSize = 13.sp)
                    if (p.amount != null) {
                        Text(text = "Amount: ₹${p.amount} ${p.currency}", color = MatrixGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    if (p.note != null) {
                        Text(text = "Note: ${p.note}", color = CyberTextSecondary, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(report.raw))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No UPI app found on device", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF88)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("PAY VIA UPI APP", color = Color.Black, fontFamily = MonospaceFont, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        is ParsedPayload.VCard -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFBD00FF), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color(0xFFBD00FF))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "VCARD CONTACT PROFILE", color = Color(0xFFBD00FF), fontFamily = MonospaceFont, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = p.formattedName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    p.phoneNumbers.forEach { phone ->
                        Text(text = "Phone: $phone", color = CyberTextPrimary, fontFamily = MonospaceFont, fontSize = 13.sp)
                    }
                    p.emails.forEach { email ->
                        Text(text = "Email: $email", color = CyberTextPrimary, fontFamily = MonospaceFont, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI).apply {
                                    putExtra(ContactsContract.Intents.Insert.NAME, p.formattedName)
                                    if (p.phoneNumbers.isNotEmpty()) {
                                        putExtra(ContactsContract.Intents.Insert.PHONE, p.phoneNumbers.first())
                                    }
                                    if (p.emails.isNotEmpty()) {
                                        putExtra(ContactsContract.Intents.Insert.EMAIL, p.emails.first())
                                    }
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Cannot open contacts", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD00FF)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ADD TO CONTACTS", color = Color.White, fontFamily = MonospaceFont, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        else -> {
            // General text or other payload
        }
    }
}
