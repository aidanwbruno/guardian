package com.vdevcode.guardian.fragments


import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.media.AudioManager
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.sac.speech.GoogleVoiceTypingDisabledException
import com.sac.speech.Speech
import com.sac.speech.SpeechDelegate
import com.sac.speech.SpeechRecognitionNotAvailable
import com.vdevcode.guardian.R
import com.vdevcode.guardian.extensions.bg
import com.vdevcode.guardian.helpers.ConstantHelper
import com.vdevcode.guardian.helpers.Guardian
import com.vdevcode.guardian.helpers.Helper
import kotlinx.android.synthetic.main.fragment_main.*
import java.util.*
import kotlin.collections.ArrayList


class MainFragment : BaseFragment(R.layout.fragment_main, "Guardian App"), SpeechDelegate, Speech.stopDueToDelay {

    private var voiceText = ""
    private var words = ArrayList<String>()
    private var ouvindo = false;


    /*
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        MainActivity.toolbar(requireActivity() as AppCompatActivity, "BCAgnello", R.drawable.ic_menu)
        Speech.init(context, context?.packageName)
        //Speech.getInstance().setPreferOffline(true)
        Speech.getInstance().setLocale(Locale("pt", "BR"))
        Speech.getInstance().setListener(this)
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

     */

    override fun setupParams() {
        super.setupParams()
    }

    override fun buildFragment() {
        arguments?.let {
            it.getStringArrayList("words")?.let {
                words = it
            }
        }

        Guardian.requestAppPermissions(
            this,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        enableAutoStart()

        setupButtons()
    }

    // Cards Views Clicables
    override fun setupButtons() {
        efab_start_listening.setOnClickListener {
            startListening()
        }
        fab_about_app.setOnClickListener {
            findNavController().navigate(R.id.action_goto_about_app)
        }
    }


    // verificar se o result ta on antes de parar de ouvir, pois as vezes demora uns segundionhos
    fun processResult() {
        val map = mutableMapOf<String, Int>()
        val result = voiceText.trim().split(" ")
        result.forEach { p ->
            words.forEach { w ->
                if (!p.isBlank() && !w.isBlank() && p.equals(w, true)) {
                    if (map.containsKey(p.toLowerCase())) {
                        val cont = map.getValue(p.toLowerCase())
                        map.put(p.toLowerCase(), cont + 1)
                    } else {
                        map.put(p.toLowerCase(), 1)
                    }
                }
            }
        }
        var textFinal = "Resultado do Teste: \n"
        map.forEach {
            textFinal = textFinal.plus("${it.key} ( ${it.value} vezes )\n")
        }
        // tv_text_said.text = textFinal
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


    fun startListening() {
        if (ouvindo) {
            Speech.getInstance().shutdown()
            efab_start_listening.bg(android.R.color.holo_red_dark)
            efab_start_listening.setImageDrawable(context?.getDrawable(R.drawable.ic_audio_off))
            tv_guardian_status.text = "INICIAR GUARDIAN"
            processResult()
            unmuteBeep()
            ouvindo = false
        } else {
            ouvindo = true
            try {
                Speech.init(context, context?.packageName)
                Speech.getInstance().setLocale(Locale("pt", "BR"))
                Speech.getInstance().setListener(this)
                //tv_text_said.text = ""
                voiceText = ""
                efab_start_listening.setBackgroundColor(ActivityCompat.getColor(context!!, android.R.color.holo_green_dark))
                efab_start_listening.setImageDrawable(context?.getDrawable(R.drawable.ic_audio_on))
                tv_guardian_status.text = "GUARDIAN INICIADO"
                Speech.getInstance().stopTextToSpeech()
                Speech.getInstance().startListening(null, this)

            } catch (ex: SpeechRecognitionNotAvailable) {
                Helper.LogE("Speech not Available")
                Guardian.dialog("", "O Reconhecimento de Voz do seu aparelho está desativado não existe").setPositiveButton("Abrir Connfiguurações", DialogInterface.OnClickListener { dialogInterface, i ->
                    Guardian.openVoiceSettings()
                }).create().show()
            } catch (go: GoogleVoiceTypingDisabledException) {
                Helper.LogE("Google Typing error")
                Guardian.dialog("", "A digitação por voz do seu aparelho está desativada").setPositiveButton("Abrir Connfiguurações", DialogInterface.OnClickListener { dialogInterface, i ->
                    Guardian.openVoiceSettings()
                }).create().show()
            }
            //muteBeep()
        }
    }


    override fun onStartOfSpeech() {
    }

    override fun onSpeechPartialResults(results: MutableList<String>?) {
        Helper.LogI("Parial: ${results.toString()}")

        /*results?.forEach { result ->
            if (!result.isBlank() && result.length > 1 && !result.equals("\n")) {
                val t = tv_text_said.text.toString()
                voiceText = voiceText.plus(" $result ")
                //tv_text_said.text = voiceText
            }
        }

         */
    }

    override fun onSpeechRmsChanged(value: Float) {
        //  Helper.LogI("RMS: ${value}")
    }

    override fun onSpeechResult(result: String?) {
        if (!result.isNullOrBlank() && result.length > 1 && !result.equals("\n")) {
            // val t = tv_text_said.text.toString()
            voiceText = voiceText.plus(" $result ")
            //tv_text_said.text = voiceText
        }
        Helper.LogI("RESULT : $voiceText")
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
                Guardian.dialog("", "O Reconhecimento de Voz do seu aparelho está desativado não existe").setPositiveButton("Abrir Connfiguurações", DialogInterface.OnClickListener { dialogInterface, i ->
                    Guardian.openVoiceSettings()
                }).create().show()
            } catch (go: GoogleVoiceTypingDisabledException) {
                Helper.LogE("Google Typing error")
                Guardian.dialog("", "A digitação por voz do seu aparelho está desativada").setPositiveButton("Abrir Connfiguurações", DialogInterface.OnClickListener { dialogInterface, i ->
                    Guardian.openVoiceSettings()
                }).create().show()
            } catch (ex: Exception) {
                Helper.LogE("Erro on COmand")
                ex.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
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

