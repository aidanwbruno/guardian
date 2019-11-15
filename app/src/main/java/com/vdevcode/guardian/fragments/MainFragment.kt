package com.vdevcode.guardian.fragments


import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.media.AudioManager
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.gson.Gson
import com.sac.speech.GoogleVoiceTypingDisabledException
import com.sac.speech.Speech
import com.sac.speech.SpeechDelegate
import com.sac.speech.SpeechRecognitionNotAvailable
import com.vdevcode.guardian.R
import com.vdevcode.guardian.auth.AppAuth
import com.vdevcode.guardian.database.AppFireDB
import com.vdevcode.guardian.helpers.*
import com.vdevcode.guardian.models.Alert
import kotlinx.android.synthetic.main.fragment_main.*
import java.util.*
import kotlin.collections.ArrayList


class MainFragment : BaseFragment(R.layout.fragment_main, "Guardian App", false, R.drawable.ic_security), SpeechDelegate, Speech.stopDueToDelay {

    private var voiceText = ""
    private var words = ArrayList<String>()
    private var ouvindo = false
    private var commandTemp = 0
    private val googleLocationHelper = GoogleLocationHelper()


    override fun setupParams() {
        Speech.init(context, context?.packageName)
        //Speech.getInstance().setPreferOffline(true)
        Speech.getInstance().setLocale(Locale("pt", "BR"))
        //Speech.getInstance().setPreferOffline(true)
        // Speech.getInstance().setTransitionMinimumDelay(5000)
        //Speech.getInstance().setStopListeningAfterInactivity()TransitionMinimumDelay(5000)
        Speech.getInstance().setListener(this)

        Helper.LogE("LISTENING ? : ${Speech.getInstance()?.isListening}")
    }

    override fun buildFragment() {
        Guardian.requestAppPermissions(
            this,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        enableAutoStart()

        setupButtons()

        //Guardian.toast("Localização pela rede: Lat: ${LocationHelper.getNetworkLocation(context!!)?.longitude}, Long: ${LocationHelper.getNetworkLocation(context!!)?.longitude}")

        // LocationHelper.checkUserGPS(context!!)
        googleLocationHelper.init(context!!)
        googleLocationHelper.startLocationUpdates(activity!!) // get the locations

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        googleLocationHelper.checkFixResult(requestCode, resultCode)
    }

    override fun onResume() {
        super.onResume()
        ouvindo = false//Guardian.getPrefDB()?.getBoolean(ConstantHelper.APP_STATUS_LISTENING, false)!!
        Guardian.getPrefDB()?.edit()?.putBoolean(ConstantHelper.APP_STATUS_LISTENING, false)?.apply()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            ConstantHelper.APP_PERMISSION_REQ_CODE -> {
                for (p in 0..permissions.size - 1) {
                    if (permissions[p] == Manifest.permission.ACCESS_FINE_LOCATION && grantResults[p] == PackageManager.PERMISSION_GRANTED) {
                        //val location = LocationHelper.getGpsLocation(context!!)
                        googleLocationHelper.startLocationUpdates(activity!!)
                    } else {
                        Guardian.requestSinglePermission(activity!!, Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
            }
        }
    }


    // Cards Views Clicables
    override fun setupButtons() {

        efab_start_listening.setOnClickListener {
            ouvindo = Guardian.getPrefDB()?.getBoolean(ConstantHelper.APP_STATUS_LISTENING, false)!!
            ouvindo = !ouvindo
            if (ouvindo) {
                startListening()
                Guardian.getPrefDB()?.edit()?.putBoolean(ConstantHelper.APP_STATUS_LISTENING, ouvindo)?.apply()

            } else {
                stopListening()
                Guardian.getPrefDB()?.edit()?.putBoolean(ConstantHelper.APP_STATUS_LISTENING, ouvindo)?.apply()
            }

        }
        fab_about_app.setOnClickListener {
            findNavController().navigate(R.id.action_goto_about_app)
        }
    }


    override fun onStop() {

        try {
            // if (Speech.getInstance().isListening) {
            //Speech.getInstance().shutdown()
            //}
            //unmuteBeep()
        } catch (ex: Exception) {
            Helper.LogE("Erro no Stop: $words")
        }
        super.onStop()
    }


    fun stopListening() {
        if (Speech.getInstance() != null) {
            Speech.getInstance().shutdown()
            efab_start_listening.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context!!, android.R.color.holo_red_dark)))
            efab_start_listening.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_audio_off))
            tv_guardian_status.text = "INICIAR o GUARDIAN"
            //processResult()
            unmuteBeep()
        }
    }


    fun startListening() {
        //if (ouvindo) {
        //  stopListening()
        // } else {
        try {
            Speech.init(context, context?.packageName)
            Speech.getInstance().setLocale(Locale("pt", "BR"))
            Speech.getInstance().setListener(this)
            //tv_text_said.text = ""
            voiceText = ""
            efab_start_listening.setBackgroundTintList(ContextCompat.getColorStateList(context!!, android.R.color.holo_green_dark))
            efab_start_listening.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_audio_on))
            tv_guardian_status.text = "GUARDIAN CAPTANDO..."
            Speech.getInstance().stopTextToSpeech()
            Speech.getInstance().startListening(null, this)

        } catch (ex: SpeechRecognitionNotAvailable) {
            Helper.LogE("Speech not Available")
            Guardian.dialog(context!!, "", "O Reconhecimento de Voz do seu aparelho está desativado não existe").setPositiveButton("Abrir Connfiguurações", DialogInterface.OnClickListener { dialogInterface, i ->
                Guardian.openVoiceSettings()
            }).create().show()
        } catch (go: GoogleVoiceTypingDisabledException) {
            Helper.LogE("Google Typing error")
            Guardian.dialog(context!!, "", "A digitação por voz do seu aparelho está desativada").setPositiveButton("Abrir Connfiguurações", DialogInterface.OnClickListener { dialogInterface, i ->
                Guardian.openVoiceSettings()
            }).create().show()
        }
        //muteBeep()
        //}
    }


    override fun onStartOfSpeech() {
    }

    override fun onSpeechPartialResults(results: MutableList<String>?) {
    }

    override fun onSpeechRmsChanged(value: Float) {
        //  Helper.LogI("RMS: ${value}")
    }

    override fun onSpeechResult(result: String?) {
        if (!result.isNullOrBlank() && result.length > 1 && !result.equals("\n")) {
            val normalizer = Helper.normalizeText(result)
            Helper.LogI("RESULT : $result")
            Helper.commands.forEach {
                if (normalizer.equals(it, true)) {
                    commandTemp++
                    if (commandTemp > 3) {
                        commandTemp = 0
                        //send alert to user on firebase
                        sendAlert()
                        Guardian.dialog(context!!, "ALerta", "Um ALerta foi Acionado, aqui pegamos todas as informações e mandaremos para o serividor", {}, {}, "Ok").show()
                    }
                    Guardian.toast("Achou palavra")
                }
            }
        }
    }

    private fun sendAlert() {

        val loc = Gson().toJson(googleLocationHelper.currentLocation)
        if (AppFireDB.currentAlert == null) {
            val alert = Alert().apply {
                count = 1
                open = true
                usuarioKey = AppAuth.getUserId()
            }
            AppFireDB.insertModel(alert, OnCompleteListener {
                if (it.isSuccessful) {
                    AppFireDB.currentAlert = alert
                    AppFireDB.currentAlert?.firestoreKey = it.result?.id!!
                } else {
                    AppFireDB.currentAlert = null
                }
            })
        } else {
            AppFireDB.updateCurrentAlert()
        }
    }

    override fun onSpecifiedCommandPronounced(event: String?) {
        Helper.LogI("Command : $event")

        if (ouvindo) {
            try {
                if (Speech.getInstance().isListening) {
                    Speech.getInstance().stopListening()
                    //ouvindo = false
                    //unmuteBeep()
                } else {
                    //ouvindo = true

                    Speech.getInstance().stopTextToSpeech()
                    Speech.getInstance().startListening(null, this)
                    muteBeep()

                }
            } catch (ex: SpeechRecognitionNotAvailable) {
                Helper.LogE("Speech not Available")
                Guardian.dialog(context!!, "", "O Reconhecimento de Voz do seu aparelho está desativado não existe").setPositiveButton("Abrir Connfiguurações", DialogInterface.OnClickListener { dialogInterface, i ->
                    Guardian.openVoiceSettings()
                }).create().show()
            } catch (go: GoogleVoiceTypingDisabledException) {
                Helper.LogE("Google Typing error")
                Guardian.dialog(context!!, "", "A digitação por voz do seu aparelho está desativada").setPositiveButton("Abrir Connfiguurações", DialogInterface.OnClickListener { dialogInterface, i ->
                    Guardian.openVoiceSettings()
                }).create().show()
            } catch (ex: Exception) {
                Helper.LogE("Erro on COmand")
                ex.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        try {
            Guardian.getPrefDB()?.edit()?.putBoolean(ConstantHelper.APP_STATUS_LISTENING, false)?.apply()
            if (Speech.getInstance().isListening) {
                Speech.getInstance().shutdown()
            }
            unmuteBeep()
        } catch (ex: Exception) {
            Helper.LogE("Erro no DESTROY: $words")
        }
        Helper.LogE("ON DESTROY")
        super.onDestroy()
    }

    private fun muteBeep() {
        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        audioManager?.run {
            // setStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0)
            // setStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0)
            setStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
            //setStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0)
            // setStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0)
        }
    }


    private fun unmuteBeep() {
        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        audioManager?.run {
            // setStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0)
            // setStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0)
            setStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
            //setStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0)
            // setStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0)
        }
    }


    private fun enableAutoStart() {
        ConstantHelper.AUTO_START_INTENTS.forEach {
            val resolvActivity = context?.packageManager?.resolveActivity(it, PackageManager.MATCH_DEFAULT_ONLY)
            resolvActivity?.let {

                Guardian.dialog(context!!, "", "Permissão auto start", {
                    try {
                        ConstantHelper.AUTO_START_INTENTS.forEach {
                            if (context?.packageManager?.resolveActivity(it, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                                startActivity(it);
                                return@forEach
                            }
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace();
                    }
                }, {}, "Sim").show()
            }
        }
    }

}

