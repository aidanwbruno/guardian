package com.vdevcode.guardian.services

import android.content.Intent


import android.R
import android.content.Context

import androidx.core.app.NotificationCompat
import android.app.*
import android.os.*
import android.media.AudioManager
import com.maxwell.speechrecognition.OnSpeechRecognitionListener
import com.maxwell.speechrecognition.SpeechRecognition
import com.vdevcode.guardian.helpers.Helper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.lang.Exception

//Tarefas criar uma lista de palavar para ficar ouvindo

class MaxSpeechListenerService : Service(), OnSpeechRecognitionListener {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //  setupRecognizer(intent)
        return super.onStartCommand(intent, flags, startId)
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

        setupRecognizer()
    }

    private fun setupRecognizer() {
        try {
            val speechRec = SpeechRecognition(this)
            speechRec.setSpeechRecognitionListener(this)
            speechRec.handleAudioPermissions(false)
            speechRec.startSpeechRecognition()
        } catch (ex: Exception) {
            Helper.LogE("Erro ao iniciar Max Speecg")
            ex.printStackTrace()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun OnSpeechRecognitionError(erroCode: Int, erroMessage: String?) {
        Helper.LogE("ERROR ${erroCode}, MSG: ${erroMessage}")
    }

    override fun OnSpeechRecognitionCurrentResult(result: String?) {
        Helper.LogE("FINAL RESULT ${result}")

    }

    override fun OnSpeechRecognitionFinalResult(p0: String?) {
    }

    override fun OnSpeechRecognitionStarted() {

    }

    override fun OnSpeechRecognitionStopped() {

    }

    fun initAlarm(context: Context) {
        Helper.LogI("Initi ALARME")
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, RestarVoZService::class.java)
        val alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
        alarmMgr.set(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 1000, alarmIntent
        )

    }


    private fun createNotification(context: Context): Notification {
        val builder = NotificationCompat.Builder(context, "APP_CHANNEL")
        builder.setWhen(System.currentTimeMillis())
        builder.setSmallIcon(R.drawable.ic_delete)
        builder.setContentTitle("Teste Voz")
        builder.setContentText("Voz ativo")
        builder.setOngoing(true)
        builder.setPriority(NotificationCompat.PRIORITY_MIN)
        builder.setCategory(NotificationCompat.CATEGORY_SERVICE)
        //builder.setChannelId()

        //val notifyIntent = Intent(context, DeleteActionActivity::class.java)
        //val notifyPendingIntent = PendingIntent.getActivity(context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        //builder.setContentIntent(notifyPendingIntent)

        return builder.build()
    }


    fun mute() {
        val mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mAudioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),
            0
        )
    }

}