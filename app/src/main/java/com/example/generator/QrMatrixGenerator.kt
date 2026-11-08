package com.example.generator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.File
import java.io.FileOutputStream

enum class QrEccLevel(val label: String, val zxingLevel: ErrorCorrectionLevel, val description: String) {
    LOW("L (7%)", ErrorCorrectionLevel.L, "High density / Low redundancy"),
    MEDIUM("M (15%)", ErrorCorrectionLevel.M, "Balanced redundancy"),
    QUARTILE("Q (25%)", ErrorCorrectionLevel.Q, "High resistance to damage"),
    HIGH("H (30%)", ErrorCorrectionLevel.H, "Maximum error correction")
}

object QrMatrixGenerator {

    fun generateQrBitmap(
        content: String,
        eccLevel: QrEccLevel = QrEccLevel.MEDIUM,
        foregroundColor: Int = Color.parseColor("#00F0FF"),
        backgroundColor: Int = Color.parseColor("#05080F"),
        size: Int = 800
    ): Bitmap {
        if (content.isBlank()) {
            val emptyBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            emptyBitmap.eraseColor(backgroundColor)
            return emptyBitmap
        }

        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to eccLevel.zxingLevel,
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.MARGIN to 1
        )

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix.get(x, y)) foregroundColor else backgroundColor
            }
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    fun saveBitmapToCache(context: Context, bitmap: Bitmap, fileName: String = "anamol_qr_${System.currentTimeMillis()}.png"): File {
        val cachePath = File(context.cacheDir, "qr_exports")
        if (!cachePath.exists()) cachePath.mkdirs()
        val file = File(cachePath, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }
}
