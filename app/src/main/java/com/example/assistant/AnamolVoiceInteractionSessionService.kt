package com.example.assistant

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService

class AnamolVoiceInteractionSessionService : VoiceInteractionSessionService() {
    override fun onNewSession(args: Bundle?): VoiceInteractionSession {
        return AnamolVoiceInteractionSession(this)
    }
}

class AnamolVoiceInteractionSession(context: Context) : VoiceInteractionSession(context) {
    override fun onHandleAssist(state: AssistState) {
        super.onHandleAssist(state)
        // Launch Circle-to-Search Screen Scanner Activity
        val intent = Intent(context, ScreenScanAssistantActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("LAUNCH_SOURCE", "ASSISTANT_TRIGGER")
        }
        context.startActivity(intent)
        hide()
    }
}
