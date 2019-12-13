package com.vdevcode.guardian.services

import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.media.AudioManager
import androidx.core.content.ContextCompat
import com.vdevcode.guardian.helpers.Guardian
import com.vdevcode.guardian.helpers.Helper


class RestarVoZService : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        intent?.action?.let {
            when (it) {
                "com.vdevcode.RESTART_SERVICE" -> {
                    if (!GuardianSpeechListenerService.serviceOn && Helper.isListening()) {
                        ContextCompat.startForegroundService(Guardian.appContext!!, Intent(context, GuardianSpeechListenerService::class.java))
                    }
                }
            }
        }
    }
}