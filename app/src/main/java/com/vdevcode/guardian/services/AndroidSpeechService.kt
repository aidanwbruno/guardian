package com.vdevcode.guardian.services

import android.content.Intent


import android.content.Context

import androidx.core.app.NotificationCompat
import android.app.*
import android.os.*
import android.media.AudioManager
import android.speech.RecognitionService
import com.maxwell.speechrecognition.SpeechRecognition
import com.sac.speech.Speech
import com.sac.speech.SpeechDelegate
import com.vdevcode.guardian.helpers.Helper
import java.lang.Exception
import com.sac.speech.GoogleVoiceTypingDisabledException
import com.sac.speech.SpeechRecognitionNotAvailable
import java.util.*


//Tarefas criar uma lista de palavar para ficar ouvindo

class AndroidSpeechService : RecognitionService() {


    override fun onStartListening(p0: Intent?, p1: Callback?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCancel(p0: Callback?) {
        Helper.LogE("Ovindo")
    }

    override fun onStopListening(p0: Callback?) {
        Helper.LogE("Parou de Ouvir")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return super.onStartCommand(intent, flags, startId)
    }


    private fun muteBeepSound() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        audioManager?.run {
            // setStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0)
            // setStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0)
            setStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
            //setStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0)
            // setStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        restartService(this)
        super.onTaskRemoved(rootIntent)
    }


    fun restartService(context: Context) {
        Helper.LogI("Initi ALARME")
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = PendingIntent.getService(context, Random().nextInt(), Intent(context, AndroidSpeechService::class.java), PendingIntent.FLAG_ONE_SHOT)
        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 2000, intent)
    }

    override fun onCreate() {
        super.onCreate()
        Helper.LogI("CREATE SERVICE")


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val CHANNEL_ID = "my_app"
            val channel = NotificationChannel(
                CHANNEL_ID,
                "MyApp", NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("")
                .setContentText("")
                .setSound(null)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true).build()
            startForeground(1, notification)
            manager.cancel(1) // remove
        }

    }


    override fun onDestroy() {
        super.onDestroy()
    }


}