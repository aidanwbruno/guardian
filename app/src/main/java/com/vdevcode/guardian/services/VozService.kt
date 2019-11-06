package com.vdevcode.guardian.services

import android.app.Service
import android.content.Intent
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.*
import android.app.PendingIntent
import android.R
import android.app.AlarmManager
import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.*
import android.media.AudioManager
import com.vdevcode.guardian.helpers.Helper
import com.vdevcode.guardian.helpers.Guardian


//default

class VozService : Service(), RecognitionListener {

    private lateinit var speechRec: SpeechRecognizer
    private var listening = false
    private val repeat = Handler()
    private lateinit var ruuu: Runnable

    override fun onCreate() {
        super.onCreate()
        speechRec = SpeechRecognizer.createSpeechRecognizer(this)
        speechRec.setRecognitionListener(this)
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ruuu = Runnable { startListening() }

        repeat.postDelayed(ruuu, 4000)
        return super.onStartCommand(intent, flags, startId)
    }


    private fun startListening() {

        if (speechRec == null) {
            speechRec = SpeechRecognizer.createSpeechRecognizer(this);
            speechRec.setRecognitionListener(this);
        }
        Helper.LogI("Runnn Handler")
        val i = createRecIntent()
        when (listening) {
            true -> {
                //speechRec.stopListening()
                //listening = false
            }
            false -> {
                //speechRec.stopListening()
                //mute()
                speechRec.startListening(i)
                listening = true
            }
        }

        repeat.postDelayed(ruuu, 4000);
    }

    override fun onDestroy() {
        speechRec.destroy()
        super.onDestroy()
    }

    private fun createRecIntent(): Intent {
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        // recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 20000); // value to wait
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true); // value to wait

        recognizerIntent.putExtra(
            RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,
            20000
        ); // value to wait
        // recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 25000); // value to wait
        // recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 20000); // value to wait
        recognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
            Locale("pt", "BR")
        ) // work for offline
        recognizerIntent.putExtra(
            RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES,
            "pt-BR, en-US"
        ) /// work for onliny
        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault()) /// work for onliny
        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity?.packageName);
        recognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.packageName);
        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        // recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        return recognizerIntent;
    }

    override fun onReadyForSpeech(args: Bundle?) {
        //Helper.LogI("READY FOR SPEACH")
    }

    override fun onRmsChanged(p0: Float) {
        // Helper.LogI("ON RMS CHANGED")

    }

    override fun onBufferReceived(p0: ByteArray?) {
        // Helper.LogI("BUFFER RECEIVED")
    }

    override fun onPartialResults(results: Bundle?) {
    }

    override fun onEvent(p0: Int, p1: Bundle?) {
        // Helper.LogI("ON EVENTS")
    }

    override fun onBeginningOfSpeech() {
        // Helper.LogI("ON BEGINNINF OF SPEECH")
    }

    override fun onEndOfSpeech() {
        // Helper.LogI("ON END OF SPEECH")
    }

    override fun onError(erroCode: Int) {
        Helper.LogI("ON ERROR OF SPEECH $erroCode")
        listening = false
    }

    override fun onResults(results: Bundle?) {
        //Helper.LogI("ON RESULTS OF SPEECH")

        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        var text = "";
        matches?.forEach {
            text += "$it \n"
        }
        // tv_voice.text = matches?.get(0)
        runCommand(matches?.get(0).toString().trim().toLowerCase())
        Helper.LogW("RESULT COMPLETE: $text")
        listening = false
        speechRec.stopListening()
    }


    private fun runCommand(key: String) {
        if (key.equals("mostrar mensagem", true)) {
            Guardian.toast("Comando Mensagem 1")
            return;
        }

        if (key.contains("mudar cor branc", true)) {
            Guardian.toast("Comando Mensagem")
            return;
        }

        if (key.equals("mudar cor azul", true)) {
            Guardian.toast("Comando Mensagem 3 ")
            return
        }

        if (key.equals("mudar cor laranja", true)) {
            Guardian.toast("Comando Mensagem 4")
            return
        }

        if (key.equals("mudar cor verde", true)) {
            Guardian.toast("Comando Mensagem 5")
            return
        }

        if (key.equals("mudar cor vermelha", true)) {
            Guardian.toast("Comando Mensagem 6")
            return
        }

        if (key.equals("mudar cor preto", true)) {
            Guardian.toast("Comando Mensagem 7")
            return
        }

        if (key.equals("mudar cor", true)) {
            Guardian.toast("Comando Mensagem: Cor random")
            return
        }
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