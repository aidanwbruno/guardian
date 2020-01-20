package com.vdevcode.guardian.services


import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.sac.speech.GoogleVoiceTypingDisabledException
import com.sac.speech.Speech
import com.sac.speech.SpeechDelegate
import com.sac.speech.SpeechRecognitionNotAvailable
import com.vdevcode.guardian.auth.AppAuth
import com.vdevcode.guardian.database.AppFireDB
import com.vdevcode.guardian.helpers.GoogleLocationHelper
import com.vdevcode.guardian.helpers.Guardian
import com.vdevcode.guardian.helpers.Helper
import com.vdevcode.guardian.models.Alert
import com.vdevcode.guardian.models.Command
import com.vdevcode.guardian.repo.AppRepo
import com.vdevcode.guardion.helpers.AudioFileHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.*


//Tarefas criar uma lista de palavar para ficar ouvindo

class GuardianSpeechListenerService : Service(), SpeechDelegate, Speech.stopDueToDelay {

    private lateinit var speechDelegate: SpeechDelegate
    private var ouvindo = false
    private var musicOn = false
    private var speechOn: Boolean? = null
    private var micriphoneOn: Boolean? = null
    private var volume = false
    private var audioManager: AudioManager? = null
    private var myCommands = mutableMapOf<Long, String>()
    private var cont = 0
    private var recording = false

    companion object {
        var serviceOn = false
    }

    override fun onCreate() {
        super.onCreate()
        Helper.LogI("CREATE SERVICE")
        serviceOn = true
        // speechDelegate = this
        ouvindo = Helper.isListening()
        // if (ouvindo) {
        runSpeechListener()
        start()
        // }
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        Helper.showServiceNotification(this)
        getMyCommands()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Helper.LogI("CREATE SERVICE COMMAND")
        getMyCommands()
        serviceOn = true
        // speechDelegate = this
        ouvindo = Helper.isListening()

        if (audioManager == null) {
            audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        }
        if (ouvindo)
            Helper.showServiceNotification(this)

        runSpeechListener()
        // start()

        return super.onStartCommand(intent, flags, startId) //START_STICKY
    }

    fun runSpeechListener() {
        if (Helper.isListening()) {
            GlobalScope.launch(Dispatchers.Main) {
                initSpeech()
                startListening()
                //Helper.LogE("SERVICE HAS GOAL ?")
            }
        } else {
            Helper.LogE("SERVICE HAS NO GOAL ?")
            shutdownListening()
        }
    }

    private fun initSpeech() {
        try {
            speechDelegate = this
            Speech.init(this, packageName)
            //Speech.getInstance().setLocale(Locale("pt", "BR"))
            Speech.getInstance().setListener(this)
            Helper.LogW("SSpeech Init")
        } catch (ex: Exception) {
            Helper.LogW("SSpeech Init ERROR ")
            ex.printStackTrace()
        }
    }

    fun startListening() {
        // check microphone is
        val micOk = checkMicrophoneIsAvailable()
        if (!micOk) {
            micriphoneOn = true
            speechOn = false
            ouvindo = false
        } else {
            try {
                muteBeep()
                Speech.getInstance().stopTextToSpeech()
                Speech.getInstance().startListening(null, this)
                muteBeep()
                speechOn = true
                ouvindo = true
                // }
            } catch (ex: SpeechRecognitionNotAvailable) {
                Helper.LogE("Speech not Available")
                speechOn = false
                ouvindo = false
            } catch (go: GoogleVoiceTypingDisabledException) {
                Helper.LogE("Google Typing error")
            } catch (ex: Exception) {
                Helper.LogE("start exxx")
                speechOn = false
                ouvindo = false
            }
        }
    }

    fun stopListening() {
        try {
            if (Speech.getInstance() != null) {
                Speech.getInstance().shutdown()
            }
        } catch (ex: IllegalStateException) {
            ex.printStackTrace()
            GlobalScope.launch(Dispatchers.Main) {
                Speech.init(this@GuardianSpeechListenerService)
                Speech.getInstance().shutdown()
            }
            Helper.LogW("Pausando Guardian")
        }
        speechOn = false
        ouvindo = false

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Helper.LogI("UNBIND")
        return super.onUnbind(intent)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        speechOn = false
        serviceOn = false
        if (Helper.isListening()) {
            restartService(this, true)
        } else {
            shutdownListening()
            stopSelf()
        }

        super.onTaskRemoved(rootIntent)
    }

    override fun onStartOfSpeech() {
        // Helper.LogI("START SPEECH")
    }

    override fun onSpeechPartialResults(results: MutableList<String>?) {
        //Helper.LogI("Parial: ${results.toString()}")
    }

    override fun onSpeechRmsChanged(value: Float) {
    }

    override fun onSpeechResult(result: String?) {
        if (!result.isNullOrBlank() && result.length > 1 && !result.equals("\n")) {
            val normalizer = Helper.normalizeText(result)
            myCommands.forEach {
                if (normalizer.contains(it.value, true)) {
                    sendAlert()
                    Guardian.toast("Um Alerta foi Acionado, aqui pegamos todas as informações e mandaremos para o serividor")
                }
            }
        }
        //Helper.LogI("RESUL SPEECH")
    }

    override fun onSpecifiedCommandPronounced(event: String?) {
        Helper.LogI("Command : $event")
        cont = 0
        if (ouvindo && !musicOn && !recording) { // loop
            try {
                if (Speech.getInstance().isListening) {
                    Speech.getInstance().stopListening()
                    Helper.LogE("UM MUTE")
                    speechOn = false
                    //unmuteBeep()
                } else {
                    muteBeep()
                    Speech.getInstance().stopTextToSpeech()
                    Speech.getInstance().startListening(null, speechDelegate)
                    //startListening()
                    speechOn = true
                    ouvindo = true
                    muteBeep()
                }
            } catch (ex: SpeechRecognitionNotAvailable) {
                Helper.LogE("Command - Speech not Available")
                speechOn = false
            } catch (go: GoogleVoiceTypingDisabledException) {
                Helper.LogE("Command - Google Typing error")
            } catch (ex: Exception) {
                Helper.LogE("Erro on COmand")
                musicOn = true
                speechOn = false
                ouvindo = false
                ex.printStackTrace()
            }
        }
    }

    fun restartService(context: Context, boadcast: Boolean) {
        Helper.LogI("Init ALARME")
        if (boadcast) {
            val intent = Intent(this, RestarVoZService::class.java).apply {
                action = "com.vdevcode.RESTART_SERVICE"
            }
            sendBroadcast(intent)
        } else {
            val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = PendingIntent.getService(context, Random().nextInt(), Intent(context, GuardianSpeechListenerService::class.java), PendingIntent.FLAG_ONE_SHOT)
            alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 2000, intent)
        }
    }

    fun shutdownListening() {
        stopListening()
        Helper.setListening(false)
        unmuteBeep()
        this.onDestroy()
    }

    private fun getMyCommands() {
        GlobalScope.launch(Dispatchers.IO) {
            val list = AppRepo.getAllCommands() as MutableList<Command>?
            list?.forEach {
                myCommands.put(it.comandoId, it.palavra)
            }
        }
    }

    override fun onDestroy() {
        Helper.LogE("SERVIÇO DESTRUIDO")
        // stop()
        serviceOn = false
        speechOn = false
        // Helper.setListening(false)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(1)
        if (Helper.isListening()) {
            restartService(this, true)
        } else {
            stop()
            stopSelf()
        }
        //stopForeground(true)
        //unregisterReceiver(RestarVoZService::class)
        super.onDestroy()
    }

    private fun checkMusicIsOff() {
        cont++
        val music = Helper.checkMusic()

        if (music) {
            cont = 0
            ouvindo = false
            //speechOn = false
            if (!musicOn) {
                stopListening()
            }

            val vol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)

            Helper.LogE("MUSICA.. ATIVA ${vol}")
            vol?.let {
                if (vol <= 0) {
                    unmuteBeep()
                }
            }
            musicOn = true

        } else {
            volume = false
            ouvindo = true
            if (musicOn) {
                Helper.LogE("MUSICA PAROU..")
                musicOn = false
                //onDestroy()
                runSpeechListener()
                //startListening()
            }
        }

        try {
            // if ((speechOn != null && speechOn == false && Helper.isListening()) || cont > 10) {
            if (cont > 5 && !recording) {
                Helper.LogE("SPEECH NOT LISTENINF..")
                cont = 2
                speechOn = true
                runSpeechListener()

                // startListening()
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun muteBeep() {
        //val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        audioManager?.run {
            setStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
        }
    }

    private fun unmuteBeep() {
        audioManager?.run {
            setStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
        }
    }

    private var timer: Timer? = null
    private val timerTask: TimerTask = object : TimerTask() {
        override fun run() {
            if (!recording) {
                checkMusicIsOff()
            }
            //checkMicrophoneIsOne()
        }
    }

    fun start() {
        if (Helper.isListening()) {
            if (timer != null) {
                return
            }
            timer = Timer()
            timer?.scheduleAtFixedRate(timerTask, 500, 1500)
        } else {
            stop()
        }
    }

    fun stop() {
        timer?.cancel()
        timer = null
    }


    // Work 90%
    private fun checkMicrophoneIsAvailable(): Boolean {
        var available = true
        var recorder = AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_DEFAULT, 44100) as AudioRecord?
        try {
            recorder?.run {
                // if (getRecordingState() != AudioRecord.RECORDSTATE_STOPPED) {
                //available = false
                //}
                startRecording()
                if (getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    stop()
                    available = true
                } else {
                    available = false
                }
            }
        } finally {
            recorder?.release();
            recorder = null
        }
        Helper.LogE("MICROFONE OFF ${available}")
        return available
    }

    private fun sendAlert() {
        //val loc = Gson().toJson(googleLocationHelper.currentLocation)
        if (AppFireDB.currentAlert == null) {
            val alert = Alert().apply {
                count = 1
                open = true
                audio = "gravando audio..."
                usuarioKey = AppAuth.getUserId()
            }
            AppFireDB.insertModel(alert, OnCompleteListener {
                if (it.isSuccessful) {
                    AppFireDB.currentAlert = alert
                    AppFireDB.currentAlert?.firestoreKey = it.result?.id!!
                    if (alert.audio.isBlank() || alert.audio.contains("gravan", true)) {
                        GlobalScope.launch(Dispatchers.Main) {
                            recording = true
                            stopListening()
                            delay(2000)
                            Helper.LogW("Iniciando Gravação de Audio")
                            AudioFileHelper.startRecordAudio(this@GuardianSpeechListenerService) {
                                recording = false
                                Helper.LogW("Gravação de Audio Completa Enviando ao Firebase")
                            }
                        }
                    }
                } else {
                    AppFireDB.currentAlert = null
                }
            })
        } else {
            AppFireDB.updateCurrentAlert(null)
        }

    }


}