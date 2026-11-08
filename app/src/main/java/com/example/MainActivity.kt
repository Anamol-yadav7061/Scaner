package com.example

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ScreenSearchDesktop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.analyzer.HackerDataInspector
import com.example.assistant.ScreenScanAssistantActivity
import com.example.ui.screens.GeneratorScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.InspectorScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AnamolQRTheme
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberThemePalette
import com.example.ui.theme.MonospaceFont
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.OledBlack
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch

enum class AppNavScreen {
    SCANNER,
    GENERATOR,
    HISTORY,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private var initialRawPayload: String? = null
    private var initialFormat: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIncomingIntent(intent)

        setContent {
            var currentPalette by remember { mutableStateOf(CyberThemePalette.CYBER_CYAN) }

            AnamolQRTheme(palette = currentPalette) {
                MainAppContent(
                    initialRaw = initialRawPayload,
                    initialFormat = initialFormat,
                    currentPalette = currentPalette,
                    onPaletteChange = { currentPalette = it },
                    onLaunchScreenScanner = {
                        val intent = Intent(this, ScreenScanAssistantActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return

        if (intent.hasExtra("INSPECT_RAW")) {
            initialRawPayload = intent.getStringExtra("INSPECT_RAW")
            initialFormat = intent.getStringExtra("INSPECT_FORMAT") ?: "QR_CODE"
        } else if (intent.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true) {
            val imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
            if (imageUri != null) {
                decodeSharedImage(imageUri)
            }
        }
    }

    private fun decodeSharedImage(uri: Uri) {
        try {
            val inputImage = InputImage.fromFilePath(this, uri)
            val scanner = BarcodeScanning.getClient()
            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        val b = barcodes.first()
                        val raw = b.rawValue ?: b.displayValue ?: ""
                        initialRawPayload = raw
                        initialFormat = b.format.toString()
                        recreate()
                    } else {
                        Toast.makeText(this, "No barcode detected in shared image", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to analyze image", Toast.LENGTH_SHORT).show()
                }
        } catch (_: Exception) {}
    }
}

@Composable
fun MainAppContent(
    initialRaw: String?,
    initialFormat: String?,
    currentPalette: CyberThemePalette,
    onPaletteChange: (CyberThemePalette) -> Unit,
    onLaunchScreenScanner: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val app = context.applicationContext as? AnamolApplication

    var currentScreen by remember { mutableStateOf(AppNavScreen.SCANNER) }
    var inspectingPayload by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(initialRaw, initialFormat) {
        if (!initialRaw.isNullOrBlank()) {
            inspectingPayload = Pair(initialRaw, initialFormat ?: "QR_CODE")
        }
    }

    // Handle Back Press when in Inspector
    BackHandler(enabled = inspectingPayload != null) {
        inspectingPayload = null
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(OledBlack),
        containerColor = OledBlack,
        bottomBar = {
            if (inspectingPayload == null) {
                NavigationBar(
                    containerColor = CyberCard,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .border(1.dp, CyberCardBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppNavScreen.SCANNER,
                        onClick = { currentScreen = AppNavScreen.SCANNER },
                        icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(22.dp)) },
                        label = { Text(stringResource(R.string.nav_scanner), fontFamily = MonospaceFont, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan,
                            unselectedIconColor = CyberTextSecondary,
                            unselectedTextColor = CyberTextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = onLaunchScreenScanner,
                        icon = { Icon(Icons.Default.ScreenSearchDesktop, contentDescription = null, modifier = Modifier.size(22.dp)) },
                        label = { Text(stringResource(R.string.nav_screen_scan), fontFamily = MonospaceFont, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = Color(0xFFBD00FF),
                            unselectedTextColor = Color(0xFFBD00FF)
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppNavScreen.GENERATOR,
                        onClick = { currentScreen = AppNavScreen.GENERATOR },
                        icon = { Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(22.dp)) },
                        label = { Text(stringResource(R.string.nav_generator), fontFamily = MonospaceFont, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan,
                            unselectedIconColor = CyberTextSecondary,
                            unselectedTextColor = CyberTextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppNavScreen.HISTORY,
                        onClick = { currentScreen = AppNavScreen.HISTORY },
                        icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(22.dp)) },
                        label = { Text(stringResource(R.string.nav_history), fontFamily = MonospaceFont, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan,
                            unselectedIconColor = CyberTextSecondary,
                            unselectedTextColor = CyberTextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppNavScreen.SETTINGS,
                        onClick = { currentScreen = AppNavScreen.SETTINGS },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(22.dp)) },
                        label = { Text(stringResource(R.string.nav_settings), fontFamily = MonospaceFont, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan,
                            unselectedIconColor = CyberTextSecondary,
                            unselectedTextColor = CyberTextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val payloadToInspect = inspectingPayload
            if (payloadToInspect != null) {
                InspectorScreen(
                    rawContent = payloadToInspect.first,
                    format = payloadToInspect.second,
                    onBack = { inspectingPayload = null }
                )
            } else {
                when (currentScreen) {
                    AppNavScreen.SCANNER -> {
                        ScannerScreen(
                            onCodeScanned = { raw, format ->
                                val report = HackerDataInspector.inspect(raw, format)
                                coroutineScope.launch {
                                    app?.repository?.recordScan(
                                        rawContent = raw,
                                        format = format,
                                        parsedType = report.mimeType,
                                        securityRating = report.threatLevel.name,
                                        title = raw.take(35)
                                    )
                                }
                                inspectingPayload = Pair(raw, format)
                            },
                            onLaunchScreenScanner = onLaunchScreenScanner
                        )
                    }
                    AppNavScreen.GENERATOR -> {
                        GeneratorScreen()
                    }
                    AppNavScreen.HISTORY -> {
                        HistoryScreen(
                            onInspectItem = { raw, format ->
                                inspectingPayload = Pair(raw, format)
                            }
                        )
                    }
                    AppNavScreen.SETTINGS -> {
                        SettingsScreen(
                            currentPalette = currentPalette,
                            onPaletteChange = onPaletteChange
                        )
                    }
                }
            }
        }
    }
}
