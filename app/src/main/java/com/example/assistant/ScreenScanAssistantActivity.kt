package com.example.assistant

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Troubleshoot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AnamolApplication
import com.example.MainActivity
import com.example.analyzer.HackerDataInspector
import com.example.analyzer.InspectionReport
import com.example.analyzer.ThreatLevel
import com.example.ui.components.RotatingPiIndicator
import com.example.ui.theme.AnamolQRTheme
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.MonospaceFont
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.ThreatDanger
import com.example.ui.theme.ThreatSafe
import com.example.ui.theme.ThreatWarning
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch

class ScreenScanAssistantActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AnamolQRTheme {
                ScreenScanOverlayContent(
                    onDismiss = { finish() },
                    onOpenInspector = { raw, format ->
                        val intent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            putExtra("INSPECT_RAW", raw)
                            putExtra("INSPECT_FORMAT", format)
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun ScreenScanOverlayContent(
    onDismiss: () -> Unit,
    onOpenInspector: (String, String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val app = context.applicationContext as? AnamolApplication

    val drawnPoints = remember { mutableStateListOf<Offset>() }
    var isAnalyzing by remember { mutableStateOf(false) }
    var detectedReport by remember { mutableStateOf<InspectionReport?>(null) }
    var statusMessage by remember { mutableStateOf("CIRCLE TO SEARCH: Draw around any QR code on screen") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            isAnalyzing = true
            statusMessage = "DECODING IMAGE MATRIX..."
            try {
                val inputImage = InputImage.fromFilePath(context, uri)
                val scanner = BarcodeScanning.getClient()
                scanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        isAnalyzing = false
                        if (barcodes.isNotEmpty()) {
                            val barcode = barcodes.first()
                            val raw = barcode.rawValue ?: barcode.displayValue ?: ""
                            val format = barcode.format.toString()
                            val report = HackerDataInspector.inspect(raw, format)
                            detectedReport = report
                            app?.playScanFeedback()
                            coroutineScope.launch {
                                app?.repository?.recordScan(
                                    rawContent = raw,
                                    format = format,
                                    parsedType = report.mimeType,
                                    securityRating = report.threatLevel.name,
                                    title = "Circle-to-Search Screen Intercept"
                                )
                            }
                        } else {
                            statusMessage = "NO QR / BARCODE FOUND IN SELECTED IMAGE"
                            Toast.makeText(context, "No barcode detected in image", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        isAnalyzing = false
                        statusMessage = "ANALYSIS ERROR: ${it.localizedMessage}"
                    }
            } catch (e: Exception) {
                isAnalyzing = false
                statusMessage = "FAILED TO READ IMAGE"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
    ) {
        // Drawing canvas for Circle-to-Search
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            drawnPoints.clear()
                            drawnPoints.add(offset)
                            detectedReport = null
                            statusMessage = "CAPTURING TARGET COORDINATES..."
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            drawnPoints.add(change.position)
                        },
                        onDragEnd = {
                            if (drawnPoints.size > 5) {
                                isAnalyzing = true
                                statusMessage = "INTERCEPTING MATRIX IN SELECTED ZONE..."
                                // In Android, real screen capture requires MediaProjection or Assist screenshot.
                                // If triggered from assistant, or for demo, prompt selection or pick screenshot
                                app?.playHapticFeedback()
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        }
                    )
                }
        ) {
            if (drawnPoints.size > 1) {
                val path = Path()
                path.moveTo(drawnPoints[0].x, drawnPoints[0].y)
                for (i in 1 until drawnPoints.size) {
                    path.lineTo(drawnPoints[i].x, drawnPoints[i].y)
                }
                // Neon glow path
                drawPath(
                    path = path,
                    color = Color(0xFF00F0FF).copy(alpha = 0.4f),
                    style = Stroke(width = 18f)
                )
                drawPath(
                    path = path,
                    color = Color(0xFF00FF66),
                    style = Stroke(width = 6f)
                )
            }
        }

        // Top HUD Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(NeonCyan, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ANAMOL CIRCLE SCANNER",
                        color = NeonCyan,
                        fontFamily = MonospaceFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = statusMessage,
                    color = CyberTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = MonospaceFont
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Overlay",
                    tint = Color.White
                )
            }
        }

        // Loading HUD Indicator
        if (isAnalyzing) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                RotatingPiIndicator(
                    size = 110.dp,
                    color = NeonCyan,
                    pulseSpeedMillis = 900
                )
            }
        }

        // Bottom Helper Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 36.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Intercepted Payload Card
            AnimatedVisibility(
                visible = detectedReport != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                detectedReport?.let { report ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .border(1.dp, NeonCyan, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = CyberCard),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TARGET DECODED",
                                    color = NeonCyan,
                                    fontFamily = MonospaceFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = report.threatLevel.name,
                                    color = when (report.threatLevel) {
                                        ThreatLevel.SAFE -> ThreatSafe
                                        ThreatLevel.WARNING -> ThreatWarning
                                        ThreatLevel.DANGER -> ThreatDanger
                                    },
                                    fontFamily = MonospaceFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = report.raw,
                                color = CyberTextPrimary,
                                fontFamily = MonospaceFont,
                                fontSize = 13.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Anamol QR", report.raw))
                                        Toast.makeText(context, "Payload Copied", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy", fontSize = 12.sp, color = Color.White)
                                }

                                if (report.raw.startsWith("http://", true) || report.raw.startsWith("https://", true)) {
                                    Button(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(report.raw))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Cannot open URL", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Open", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Button(
                                    onClick = {
                                        onOpenInspector(report.raw, report.format)
                                    },
                                    modifier = Modifier.weight(1.2f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD00FF)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Troubleshoot, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Inspect", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Quick Gallery / Screenshot Button
            OutlinedButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth(0.85f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Image, contentDescription = null, tint = NeonCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SELECT SCREENSHOT TO SCAN",
                    fontFamily = MonospaceFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
