package com.vdevcode.guardian.services

import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import androidx.core.content.ContextCompat


class RestarVoZService : BroadcastReceiver() {

    override fun onReceive(context: Context?, p1: Intent?) {
        ContextCompat.startForegroundService(context!!, Intent(context, VozService::class.java))
    }
}