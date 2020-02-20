package com.vdevcode.guardian.fragments


import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.media.AudioManager
import android.speech.SpeechRecognizer
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
import com.vdevcode.guardian.extensions.mhide
import com.vdevcode.guardian.extensions.mshow
import com.vdevcode.guardian.helpers.*
import com.vdevcode.guardian.models.Alert
import com.vdevcode.guardian.repo.AppRepo
import com.vdevcode.guardion.helpers.AudioFileHelper
import com.vdevcode.guardion.helpers.FileHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


class MainFragment : BaseFragment(R.layout.fragment_main, "Guardian App", false, R.drawable.ic_security) {

    private var words = ArrayList<String>()

    override fun homeIconClicked() {
    }

    override fun createFragment() {
        // Speech.init(context, context?.packageName)
        //Speech.getInstance().setPreferOffline(true)
        // Speech.getInstance().setLocale(Locale("pt", "BR"))
        //Speech.getInstance().setPreferOffline(true)
        //Speech.getInstance().setTransitionMinimumDelay(5000)
        //Speech.getInstance().setStopListeningAfterInactivity()TransitionMinimumDelay(5000)
        //Speech.getInstance().setListener(this)

        //Helper.LogE("LISTENING ? : ${Speech.getInstance()?.isListening}")
    }

    override fun onResume() {
        super.onResume()
        requireActivity()?.ll_gps_check?.mshow()
    }

    override fun buildFragment() {

        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            //speechIntent = setupSpeechIntent()
            //speechListenerCliente = SpeechRecognizer.createSpeechRecognizer(context)
            //speechListenerCliente.setRecognitionListener(this)
            //start()
        } else {
            Guardian.dialog(context!!, "Alerta", "Seu Dispositivo não possui reconhecimento de voz, por tanto o Aplicativo não irá funcionar corretamente", {}, {}, "Ok").show()
        }
        Guardian.requestAppPermissions(this, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION)

        //enableAutoStart()

        setupButtons()

        //Guardian.toast("Localização pela rede: Lat: ${LocationHelper.getNetworkLocation(context!!)?.longitude}, Long: ${LocationHelper.getNetworkLocation(context!!)?.longitude}")


        if (Helper.isListening()) {
            Helper.startService()
            startListenerButton()
            //startListening()
            Helper.LogW("SERVICE NOT ON")
        }

    }

    private fun startListenerButton() {
        efab_start_listening.backgroundTintList = ContextCompat.getColorStateList(context!!, android.R.color.holo_green_dark)
        efab_start_listening.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_audio_on))
        tv_guardian_status.text = "GUARDIAN CAPTANDO..."
    }

    private fun stopListenerButton() {
        efab_start_listening.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, android.R.color.holo_red_dark))
        efab_start_listening.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_audio_off))
        tv_guardian_status.text = "GUARDIAN PARADO"
        Helper.unmuteBeep()
    }


    // Cards Views Clicables
    override fun setupButtons() {
        efab_start_listening.setOnClickListener {


            //AudioFileHelper.startRecordAudio(context!!) {
            //Helper.LogW("Gravação de Audio Completa Enviando ao Firebase")
            //  }
            //  return@setOnClickListener

            var ouvindo = Helper.isListening()
            ouvindo = !ouvindo
            if (ouvindo) {
                GlobalScope.launch(Dispatchers.IO) {
                    val has = async { AppRepo.getAllCommands() }.await()
                    if (has.isNullOrEmpty()) {
                        GlobalScope.launch(Dispatchers.Main) {
                            Guardian.toast("Voce não cadastrou nenhum comando")
                        }
                    } else {
                        GlobalScope.launch(Dispatchers.Main) {
                            startListenerButton()
                            Helper.setListening(ouvindo)
                            Helper.startService()
                        }
                    }
                }
            } else {
                Guardian.dialog(context!!, "", "\n \nDeseja finalizar o Guardian?", {

                    stopListenerButton()
                    Helper.setListening(false)
                    Helper.stopService()

                }, {
                    Helper.setListening(true)
                    startListenerButton()
                    //startListening() // keep listening
                }, "Finalizar", "não").show()


            }

        }
        fab_about_app.setOnClickListener {
            // AudioFileHelper.stopRecording()
            //  AudioFileHelper.playAudio(context!!, AudioFileHelper.currentAudio?.absolutePath)
            findNavController().navigate(R.id.action_goto_about_app)
        }

        fab_add_command.setOnClickListener {
            //AudioFileHelper.delete()
            // AudioFileHelper.playAudio(context!!, AudioFileHelper.currentAudio?.absolutePath)
            findNavController().navigate(R.id.action_goto_new_command)
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


    override fun onDestroy() {
        Helper.LogE("ON DESTROY")
        requireActivity()?.ll_gps_check.mhide()
        super.onDestroy()
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

