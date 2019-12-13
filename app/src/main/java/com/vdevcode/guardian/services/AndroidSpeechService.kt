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
import com.vdevcode.guardian.R
import com.vdevcode.guardian.helpers.Guardian
import com.vdevcode.guardian.models.Command
import com.vdevcode.guardian.repo.AppRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


//Tarefas criar uma lista de palavar para ficar ouvindo

class AndroidSpeechService : Service(), SpeechDelegate, Speech.stopDueToDelay {

    private lateinit var speechDelegate: SpeechDelegate
    private var ouvindo = false
    private var musicOn = false
    private var speechOk: Boolean? = null
    private var volume = false
    private var audioManager: AudioManager? = null
    private var myCommands = mutableMapOf<Long, String>()

    private fun getMyCommands() {
        GlobalScope.launch(Dispatchers.IO) {
            val list = AppRepo.getAllCommands() as MutableList<Command>?
            list?.forEach {
                myCommands.put(it.comandoId, it.palavra)
            }
        }
    }

    companion object {
        var serviceOn = false
    }

    override fun onCreate() {
        super.onCreate()
        Helper.LogI("CREATE SERVICE")
        serviceOn = true
        ouvindo = Helper.isListening()
        //if (ouvindo) {
        runSpeechListener()
        start()
        //}
        // showNotification()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        showNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Helper.LogI("CREATE SERVICE COMMAND")
        serviceOn = true

        if (audioManager == null) {
            audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        }
        /*

         runSpeechListener()
         if (ouvindo) {
             start()
         }

         */
        ouvindo = Helper.isListening()
        if (ouvindo)
            showNotification()

        runSpeechListener()

        return START_STICKY
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

    private fun showNotification() {
        val CHANNEL_ID = "my_app"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "MyApp", NotificationManager.IMPORTANCE_DEFAULT
            )

            channel.setDescription("no sound")
            channel.setSound(null, null)
            channel.enableLights(false)
            channel.enableVibration(false)

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Agnello ativo")
                .setContentText("O Serviço está em execução")
                .setSound(null)
                //.setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)

            val snoozeIntent = Intent(this, RestarVoZService::class.java).apply {
                action = "com.vdevcode.action.PAUSE_LISTENER"
                putExtra("not_id", 1)
            }
            val snoozePendingIntent: PendingIntent =
                PendingIntent.getBroadcast(this, 0, snoozeIntent, 0)
            notification.addAction(
                R.drawable.ic_audio_off, "Pausar",
                snoozePendingIntent
            )

            startForeground(1, notification.build())
        } else {
            //startService()
        }
    }

    fun runSpeechListener() {
            if (Helper.isListening()) {
                GlobalScope.launch(Dispatchers.Main) {
                    initSpeech()
                    startListening()
                    //Helper.LogE("SERVICE HAS GOAL ?")
                }
            } else {
                // Helper.LogE("SERVICE HAS NO GOAL ?")
                shutdownListening()
            }
    }

    fun stopListening() {
        try {
            if (Speech.getInstance() != null) {
                Speech.getInstance().shutdown()
                // unmuteBeep()
            }
        } catch (ex: IllegalStateException) {
            ex.printStackTrace()
            GlobalScope.launch(Dispatchers.Main) {
                Speech.init(this@AndroidSpeechService)
                Speech.getInstance().shutdown()
            }
        }
        speechOk = false
        Helper.LogE("LISTENER STOPED?")
    }

    fun startListening() {
        //Helper.checkMic(this)
        try {
            if (Speech.getInstance() == null || !Speech.getInstance().isListening) {
                Speech.init(this, packageName)
                //Speech.getInstance().setLocale(Locale("pt", "BR"))
                Speech.getInstance().setListener(this)
                Helper.LogE("INIT START")
            }
            Speech.getInstance().startListening(null, this)
            // Helper.LogE("SERVICE LISTENING ? : ${Speech.getInstance()?.isListening}")
            muteBeep()
            speechOk = true
            //listening = true

        } catch (ex: SpeechRecognitionNotAvailable) {
            Helper.LogE("Speech not Available")

        } catch (go: GoogleVoiceTypingDisabledException) {
            Helper.LogE("Google Typing error")
        } catch (ex: Exception) {
            Helper.LogE("start exxx")
        }
    }


    override fun onStartOfSpeech() {
    }

    override fun onSpeechPartialResults(results: MutableList<String>?) {
    }

    override fun onSpeechRmsChanged(value: Float) {
        //listening = true
        //Helper.LogI("RMS: ${value}")
    }

    override fun onSpeechResult(result: String?) {
        if (!result.isNullOrBlank() && result.length > 1 && !result.equals("\n")) {
            val normalizer = Helper.normalizeText(result)
            myCommands.forEach {
                if (normalizer.contains(it.value, true)) {
                    //sendAlert()
                    Guardian.toast("Um Alerta foi Acionado, aqui pegamos todas as informações e mandaremos para o serividor")
                }
            }
        }
    }

    override fun onSpecifiedCommandPronounced(event: String?) {
        Helper.LogI("Command : $event")
        if (Helper.isListening()) { // loop
            try {
                if (Speech.getInstance().isListening) {
                    Speech.getInstance().stopListening()
                    Helper.LogE("UM MUTE")
                    speechOk = false
                    //unmuteBeep()
                } else {
                    //ouvindo = true
                    muteBeep()
                    //Speech.getInstance().stopTextToSpeech()
                    Speech.getInstance().startListening(null, speechDelegate)
                    speechOk = true
                    muteBeep()
                    //listening = true
                    // Handler().postDelayed({
                    //unmuteBeep()
                    //Helper.checkMic()
                    //  }, 800)

                }
            } catch (ex: SpeechRecognitionNotAvailable) {
                Helper.LogE("Speech not Available")
                //Vocc.dialog("", "O Reconhecimento de Voz do seu aparelho está desativado não existe").setPositiveButton("Abrir Connfiguurações", DialogInterface.OnClickListener { dialogInterface, i ->
                //Vocc.openVoiceSettings()
                // }).create().show()
            } catch (go: GoogleVoiceTypingDisabledException) {
                Helper.LogE("Google Typing error")
                // Vocc.dialog("", "A digitação por voz do seu aparelho está desativada").setPositiveButton("Abrir Connfiguurações", DialogInterface.OnClickListener { dialogInterface, i ->
                //   Vocc.openVoiceSettings()
                //}).create().show()
            } catch (ex: Exception) {
                Helper.LogE("Erro on COmand")
                musicOn = true
                speechOk = false
                ex.printStackTrace()
            }
        }
    }

    ///ActivityThread: Activity com.vdevcode.vocc.activities.MainActivity has leaked ServiceConnection android.spe
    override fun onDestroy() {
        Helper.LogE("SERVIÇO DESTRUIDO")
        // stop()
        serviceOn = false
        speechOk = false
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

    override fun onTaskRemoved(rootIntent: Intent?) {
        //stop()
        speechOk = false
        //serviceOn = false
        if (Helper.isListening()) {
            restartService(this, true)
        } else {
            stop()
            stopSelf()
        }
        Helper.LogI("TASK REMOVED")
        // serviceOn = false
        super.onTaskRemoved(rootIntent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Helper.LogI("UNBIND")
        return super.onUnbind(intent)
    }

    fun restartService(context: Context, boadcast: Boolean) {
        Helper.LogI("Initi ALARME")
        if (boadcast) {
            val intent = Intent(this, RestarVoZService::class.java).apply {
                action = "com.vdevcode.RESTART_SERVICE"
            }
            sendBroadcast(intent)
        } else {
            val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = PendingIntent.getService(context, Random().nextInt(), Intent(context, AndroidSpeechService::class.java), PendingIntent.FLAG_ONE_SHOT)
            alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 2000, intent)
        }
    }

    //Caused by: java.lang.SecurityException: Permission Denial: startForeground from pid=25790, uid=10192 requires android.permission.FOREGROUND_SERVICE
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    fun shutdownListening() {
        stopListening()
        Helper.setListening(false)
        unmuteBeep()
        Helper.LogE("SHUT DONW ?")
        this.onDestroy()
    }


    private fun checkMusicisOff() {

        val music = Helper.checkMusic()
        if (music) {
            ouvindo = false
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
            }
        }

        try {
            if (speechOk != null && !speechOk!! && Helper.isListening()) {
                Helper.LogE("SPEECH NOT LISTENINF..")
                runSpeechListener()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }


    private var timer: Timer? = null
    private var noo = false
    private val timerTask: TimerTask = object : TimerTask() {
        override fun run() {
            checkMusicisOff()
            //validateMicAvailability()
        }
    }

    fun start() {
        if (ouvindo) {
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

    /*

    private fun enableAutoStart() {
        ConstantHelper.AUTO_START_INTENTS.forEach {
            val resolvActivity = context?.packageManager?.resolveActivity(it, PackageManager.MATCH_DEFAULT_ONLY)
            resolvActivity?.let {
                AlertDialog.Builder(context!!).apply {
                    setTitle("Permissão auto start")
                    setPositiveButton("Sim", DialogInterface.OnClickListener { dialogInterface, i ->
                        try {
                            ConstantHelper.AUTO_START_INTENTS.forEach {
                                if (context.packageManager?.resolveActivity(it, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                                    startActivity(it);
                                    return@forEach
                                }
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace();
                        }
                    })
                }.create().show()
            }
        }
    }

     */


    private fun validateMicAvailability() {
        val am = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val apps = am.getRunningTasks(Int.MAX_VALUE)

        apps.forEach {
            // Helper.LogW("APP LABEL ${it.baseActivity}")
        }
    }


    /*

    final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List pkgAppsList = getPackageManager().queryIntentActivities(mainIntent, 0);

        //create a package names list
        List<String> packageNames=new ArrayList<>();

        for (Object obj : pkgAppsList) {
            ResolveInfo resolveInfo = (ResolveInfo) obj;
            PackageInfo packageInfo = null;
            try {
                packageInfo = getPackageManager().getPackageInfo(resolveInfo.activityInfo.packageName, PackageManager.GET_PERMISSIONS);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            String[] requestedPermissions = packageInfo.requestedPermissions;

            //check the microphone permission
            if (requestedPermissions!=null) {
                for (String packagePermission : requestedPermissions) {
                    if (packagePermission == Manifest.permission.RECORD_AUDIO) {
                        packageNames.add(packageInfo.packageName);
                        break;
                    }
                }
            }
        }
     */

}