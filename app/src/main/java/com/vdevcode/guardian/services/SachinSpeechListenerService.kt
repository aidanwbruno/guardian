package com.vdevcode.guardian.services

import android.content.Intent


import android.content.Context

import androidx.core.app.NotificationCompat
import android.app.*
import android.os.*
import android.media.AudioManager
import com.maxwell.speechrecognition.SpeechRecognition
import com.sac.speech.Speech
import com.sac.speech.SpeechDelegate
import com.vdevcode.guardian.helpers.Helper
import java.lang.Exception
import com.sac.speech.GoogleVoiceTypingDisabledException
import com.sac.speech.SpeechRecognitionNotAvailable
import java.util.*


//Tarefas criar uma lista de palavar para ficar ouvindo

class SachinSpeechListenerService : Service(), SpeechDelegate, Speech.stopDueToDelay {


    private lateinit var speechDelegate: SpeechDelegate

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            //if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
            (Objects.requireNonNull(getSystemService(Context.AUDIO_SERVICE)) as AudioManager).adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0)
            // }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Speech.init(this);
        speechDelegate = this;
        Speech.getInstance().setListener(this)


        if (Speech.getInstance().isListening) {
            Speech.getInstance().stopListening()
            muteBeepSound()
        } else {
            try {
                Speech.getInstance().stopTextToSpeech()
                Speech.getInstance().startListening(null, this)

            } catch (ex: SpeechRecognitionNotAvailable) {
                Helper.LogE("Speech not Available")
            } catch (go: GoogleVoiceTypingDisabledException) {
                Helper.LogE("Google Typing error")
            }
            muteBeepSound()
        }

        return super.onStartCommand(intent, flags, startId)
    }


    override fun onStartOfSpeech() {
    }

    override fun onSpeechPartialResults(results: MutableList<String>?) {
        Helper.LogI("Parial: ${results.toString()}")
    }

    override fun onSpeechRmsChanged(value: Float) {
    }

    override fun onSpeechResult(result: String?) {
        Helper.LogI("RESULT : $result")
    }

    override fun onSpecifiedCommandPronounced(event: String?) {
        Helper.LogI("Command : $event")

        try {
            //if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
            (Objects.requireNonNull(getSystemService(Context.AUDIO_SERVICE)) as AudioManager).adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0)
            // }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (Speech.getInstance().isListening) {
            //  muteBeepSound()
            Speech.getInstance().stopListening()
        } else {
            try {
                Speech.getInstance().stopTextToSpeech()
                Speech.getInstance().startListening(null, this)

            } catch (ex: SpeechRecognitionNotAvailable) {
                Helper.LogE("Speech not Available")
            } catch (go: GoogleVoiceTypingDisabledException) {
                Helper.LogE("Google Typing error")
            }
            // muteBeepSound()
        }

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
        val intent = PendingIntent.getService(context, Random().nextInt(), Intent(context, SachinSpeechListenerService::class.java), PendingIntent.FLAG_ONE_SHOT)
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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun onDestroy() {
        super.onDestroy()
    }


}