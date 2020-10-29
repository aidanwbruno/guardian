package com.vdevcode.guardian.helpers

import android.app.*
import android.content.Context
import android.content.Intent
import android.icu.text.CaseMap
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.vdevcode.guardian.R
import com.vdevcode.guardian.auth.AppAuth
import com.vdevcode.guardian.services.GuardianSpeechListenerService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
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
        manager?.let { return it.isMusicActive || it.mode == AudioManager.MODE_IN_CALL }
        return false
    }

    fun showServiceNotification(context: Context, id: Int = 1, title: String = "Guardian ativo", msg: String = "O Serviço está em execução", normal: Boolean = false) {
        val CHANNEL_ID = "my_app"
        var channel: NotificationChannel? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = NotificationChannel(
                CHANNEL_ID,
                "MyApp", NotificationManager.IMPORTANCE_DEFAULT
            )

            channel?.apply {
                channel.setDescription("no sound")
                channel.setSound(null, null)
                channel.enableLights(false)
                channel.enableVibration(false)
            }
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (channel != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(msg)
            .setSound(null)
            //.setDefaults(Notification.DEFAULT_ALL)
            .setAutoCancel(true)
        if (normal == false) {
            (context as Service).startForeground(id, notification.build())
        } else {
            notification.setSmallIcon(R.drawable.app_logo)
            manager.notify(id, notification.build())
        }

    }


    fun regiterInternetCheck(context: Context) {
        if (Guardian.online)
            return
        val conn = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netReqBuilder = NetworkRequest.Builder()
        conn.registerNetworkCallback(netReqBuilder.build(), object : ConnectivityManager.NetworkCallback() {
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
                // Guardian.toast("Você está offline :( verifique sua conexão")
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

    fun checkPayment(onComplete: (ok: Boolean) -> Unit) {
        val userEmail = AppAuth.getUser()?.email ?: ""
        if (userEmail.isBlank()) {
            onComplete.invoke(false)
            return
        }
        GlobalScope.launch {
            ApiHelper.GET("https://sosguardian.app/wp-json/wc/v3/orders?consumer_key=ck_19e0f9f58ec5598f7e1187233412a0112c897ea8&consumer_secret=cs_5ef4d97790c4c02b7fadd5a611e8721a6d5973cc&status=completed&after=2020-10-01T00:01:00&per_page=20", {
                try {
                    val json = JSONArray(it)
                    json?.let {
                        for (i in 0 until it.length()) {
                            val ped = json[i] as JSONObject
                            val billing = ped.getJSONObject("billing")
                            billing?.let {
                                val email = it.getString("email")
                                email?.let {
                                    if (email.isNotBlank() && email.equals(userEmail)) {
                                        onComplete.invoke(true)
                                        return@GET
                                    }
                                }
                                LogE("EMAILS : $email")
                            }
                        }
                    }
                } catch (ex: java.lang.Exception) {
                    ex.printStackTrace()
                    //Guardian.toast("Erro ao validar o app")
                }
                onComplete.invoke(false)
            }, {
                LogE("ERROR : $it")
                //Guardian.toast("Erro se comunicar com servidor")
                onComplete.invoke(false)
            })
        }
    }

}