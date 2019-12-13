package com.vdevcode.guardian.helpers

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.vdevcode.guardian.R
import com.vdevcode.guardian.models.Command
import com.vdevcode.guardian.repo.AppRepo
import com.vdevcode.guardian.services.AndroidSpeechService
import com.vdevcode.guardian.services.GuardianSpeechListenerService
import com.vdevcode.guardian.services.RestarVoZService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.Normalizer


/**
 * Classe reponsável pelos metodos utilitarios para a aplicação.
 */
object Helper {

    private fun log(msg: String, type: Int) = when (type) {
        Log.ERROR -> Log.e(ConstantHelper.APP_LOG_TAG, msg)
        Log.INFO -> Log.i(ConstantHelper.APP_LOG_TAG, msg)
        Log.WARN -> Log.w(ConstantHelper.APP_LOG_TAG, msg)
        else -> Log.d(ConstantHelper.APP_LOG_TAG, msg)
    }

    fun LogE(msg: String) = log(msg, Log.ERROR)
    fun LogI(msg: String) = log(msg, Log.INFO)
    fun LogW(msg: String) = log(msg, Log.WARN)


    fun removeAccent(message: String): String {
        return Normalizer.normalize(message, Normalizer.Form.NFD).replace("[^\\p{ASCII}]".toRegex(), "")
    }

    fun normalizeText(text: String?): String {
        text?.let {
            val norm = removeAccent(text).toLowerCase().trim().replace("\\s+".toRegex(), " ")
            return norm.replace("[^A-Za-z ]".toRegex(), " ")
        }
        return ""
    }


    // fun addCommands() {
    val commands = arrayOf(
        "apavorado", "aqui", "baixo", "bem", "boa", "calma", "calmo", "carro", "casa", "cedo", "celular", "de", "deixa", "deixando", "deixar", "deus", "dia", "dinheiro", "documento", "em", "embaixo", "encostado", "entregando", "entregar", "estamos",
        "estava", "estou", "familia", "favor", "fazer", "ficamos", "filhas", "filhos", "fiquei", "fiquem", "fiz", "fizemos", "igreja", "indo", "ir", "levantando", "levantar", "levar", "machuca", "machuque", "mal", "me", "medo", "meu", "meus"
    )


    fun startService() {
        // stopService(context)
        try {
            val serviceIntent = Intent(Guardian.appContext!!, GuardianSpeechListenerService::class.java)
            serviceIntent.putExtra("guardian", "Foreground Android")
            ContextCompat.startForegroundService(Guardian.appContext!!, serviceIntent)
        } catch (ex: Exception) {
            Guardian.toast("Serviço de voz não iniciado, tente novamente")
        }
    }

    fun stopService() {
        val serviceIntent = Intent(Guardian.appContext!!, GuardianSpeechListenerService::class.java)
        Guardian.appContext!!.stopService(serviceIntent)
    }

    fun setListening(toogle: Boolean) {
        Guardian.getPrefDB()?.edit()?.putBoolean(ConstantHelper.APP_STATUS_LISTENING, toogle)?.apply()
    }

    fun isListening(): Boolean {
        val res = Guardian.getPrefDB()?.getBoolean(ConstantHelper.APP_STATUS_LISTENING, false)
        res?.let {
            return it
        }
        return false
    }

    fun setPaused(toogle: Boolean) {
        Guardian.getPrefDB()?.edit()?.putBoolean(ConstantHelper.APP_STATUS_LISTENING, toogle)?.apply()
    }

    fun isPaused(): Boolean {
        val res = Guardian.getPrefDB()?.getBoolean(ConstantHelper.APP_STATUS_LISTENING, false)
        res?.let {
            return it
        }
        return false
    }

    fun checkMusic(): Boolean {
        val manager = Guardian.appContext?.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        manager?.let { return it.isMusicActive || it.mode == AudioManager.MODE_IN_CALL}
        return false
    }

    fun showServiceNotification(context: Context) {
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

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Guardian ativo")
                .setContentText("O Serviço está em execução")
                .setSound(null)
                //.setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)

            /*  val snoozeIntent = Intent(context, RestarVoZService::class.java).apply {
                action = "com.vdevcode.action.PAUSE_LISTENER"
                putExtra("not_id", 1)
            }


            //val snoozePendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 0, snoozeIntent, 0)
            notification.addAction(
                R.drawable.ic_audio_off, "Pausar",
              //  snoozePendingIntent
            )


           */
            (context as Service).startForeground(1, notification.build())
        } else {
            //startService()
        }
    }


    fun regiterInternetCheck(context: Context) {
        if (Guardian.online)
            return
        val conn = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netReqBuilder = NetworkRequest.Builder()
        conn.registerNetworkCallback(
            netReqBuilder.build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network?) {
                    Guardian.online = true
                    if (isListening())
                        startService()
                    //Liso.toast("conexão de rede, restabecida!")
                }

                override fun onLost(network: Network?) {
                    Guardian.online = false
                    Guardian.toast("Você está offline :( verifique sua conexão")
                    if (isListening())
                        stopService()
                }

                override fun onLosing(network: Network?, maxMsToLive: Int) {
                    Guardian.toast("Você está offline :( verifique sua conexão")
                }

                override fun onUnavailable() {
                    Guardian.online = false
                    Guardian.toast("Rede não Disponível :(")
                }
            })
    }


    fun muteBeep() {
        val audioManager = Guardian.appContext?.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        audioManager?.run {
            setStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
        }
    }

    fun unmuteBeep() {
        val audioManager = Guardian.appContext?.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        audioManager?.run {
            setStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
        }
    }


}