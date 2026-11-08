package com.example

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.database.AppDatabase
import com.example.data.repository.ScanRepository
import java.util.Locale

class AnamolApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: ScanRepository
        private set

    private lateinit var prefs: SharedPreferences
    private var toneGenerator: ToneGenerator? = null

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        repository = ScanRepository(database.scanItemDao())
        prefs = getSharedPreferences("anamol_qr_settings", Context.MODE_PRIVATE)

        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 75)
        } catch (_: Exception) {}

        applySavedLanguage()
    }

    fun isSoundEnabled(): Boolean = prefs.getBoolean("sound_enabled", true)
    fun setSoundEnabled(enabled: Boolean) = prefs.edit().putBoolean("sound_enabled", enabled).apply()

    fun getHapticMode(): String = prefs.getString("haptic_mode", "TACTICAL") ?: "TACTICAL"
    fun setHapticMode(mode: String) = prefs.edit().putString("haptic_mode", mode).apply()

    fun getSelectedLanguage(): String = prefs.getString("app_language", "en") ?: "en"
    fun setSelectedLanguage(langCode: String) {
        prefs.edit().putString("app_language", langCode).apply()
        applyLanguage(langCode)
    }

    private fun applySavedLanguage() {
        val lang = getSelectedLanguage()
        applyLanguage(lang)
    }

    @Suppress("DEPRECATION")
    fun applyLanguage(langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    fun playScanFeedback() {
        if (isSoundEnabled()) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 120)
            } catch (_: Exception) {}
        }
        playHapticFeedback()
    }

    fun playHapticFeedback() {
        val mode = getHapticMode()
        if (mode == "OFF") return

        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        } ?: return

        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (mode) {
                "SUBTLE" -> vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                "CYBER" -> vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 35, 40, 70), -1))
                else -> vibrator.vibrate(VibrationEffect.createOneShot(55, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }
}
