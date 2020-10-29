package com.vdevcode.guardian.fragments


import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.media.AudioManager
import android.net.Uri
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
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
import com.vdevcode.guardian.services.GuardianSpeechListenerService
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
    private var appStatus = false;

    companion object {
        var ended = false
    }

    override fun homeIconClicked() {
        //Guardian.toast("Home App Guardian")
        Guardian.dialogViewMain(requireContext(), {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://sosguardian.app/")))
        }, {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://sosguardian.app/"))) // link termos de uso
        }, {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://sosguardian.app/"))) // link política de privacidade
        }).show()
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


        //todo verificar se conta tá ativva
        //appStatus = Guardian.getPrefDB()?.getBoolean("app_status", false) ?: false
        val appPayment = Guardian.getPrefDB()?.getBoolean("app_payment", false)
        var statusCount = Guardian.getPrefDB()?.getInt("app_status_count", 0) ?: 0

        if (appStatus == false) {
            Helper.LogI("Iniciando validação do app...")
            tv_checking.mshow()
            tv_checking.text = "Verificando conta..."
            requireActivity()?.ll_gps_check.mhide()
            AppFireDB.checkActivation(AppAuth.getUserId()) {
                Helper.LogI("User Ativo no firebase $it")
                if (it) {
                    Guardian.getPrefDB()?.edit()?.putBoolean("app_status", true)?.apply();
                    //Guardian.getPrefDB()?.edit()?.putBoolean("app_payment", true)?.apply()
                    ll_block.mhide()
                    tv_checking.mhide()
                    if (statusCount == 0) {
                        Guardian.getPrefDB()?.edit()?.putInt("app_status_count", 10)?.apply()
                        Guardian.dialog(requireContext(), "", "App ativado com sucesso", {}, {}, "ok").show()
                    }
                    requireActivity()?.ll_gps_check?.mshow()
                } else {
                    // show block
                    if (appPayment == false) {
                        tv_checking.text = "Verificando pagamento..."
                        Helper.checkPayment {
                            GlobalScope.launch(Dispatchers.Main) {
                                Helper.LogI("Pagamento encontrado $it...")
                                if (it) {
                                    processPayment(true)
                                } else {
                                    Guardian.getPrefDB()?.edit()?.putBoolean("app_status", false)?.apply()
                                    Guardian.getPrefDB()?.edit()?.putBoolean("app_payment", false)?.apply()
                                    tv_checking.text = "App não foi ativado"
                                    ll_block.mshow()
                                }
                            }
                        }
                    } else {
                        Helper.LogI("Pagamento encontrado")
                        processPayment(false)
                    }
                }
            }
        } else {
            ll_block.mhide()
        }

    }

    override fun buildFragment() {

        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            //speechIntent = setupSpeechIntent()
            //speechListenerCliente = SpeechRecognizer.createSpeechRecognizer(context)
            //speechListenerCliente.setRecognitionListener(this)
            //start()
        } else {
            Guardian.dialog(requireContext(), "Alerta", "Seu Dispositivo não possui reconhecimento de voz, por tanto o Aplicativo não irá funcionar corretamente", {}, {}, "Ok").show()
        }
        Guardian.requestAppPermissions(this, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION)

        //enableAutoStart()

        setupButtons()

        //Guardian.toast("Localização pela rede: Lat: ${LocationHelper.getNetworkLocation(requireContext())?.longitude}, Long: ${LocationHelper.getNetworkLocation(requireContext())?.longitude}")

        if (Helper.isListening()) {
            Helper.startService()
            startListenerButton()
            //startListening()
            Helper.LogW("SERVICE NOT ON")
        }

    }


    private fun processPayment(fromPay: Boolean) {
        Guardian.getPrefDB()?.edit()?.putBoolean("app_payment", true)?.apply()
        tv_checking.mhide()
        val snack = Snackbar.make(requireView(), "Pagamento encontrado, ativando app...", Snackbar.LENGTH_INDEFINITE)
        snack.show()
        AppFireDB.activateApp(AppAuth.getUserId()) {
            Helper.LogI("Usuário foi ativado no Firebase: $it...")
            snack.dismiss()
            if (it) {
                Guardian.getPrefDB()?.edit()?.putBoolean("app_status", true)?.apply();
                ll_block.mhide()
                requireActivity()?.ll_gps_check.mshow()
                if (fromPay) {
                    Guardian.dialog(requireContext(), "", "App ativado com sucesso", {}, {}, "ok").show()
                }
                Guardian.getPrefDB()?.edit()?.putInt("app_status_count", 10)?.apply()
            } else {
                Guardian.getPrefDB()?.edit()?.putBoolean("app_status", false)?.apply();
                Guardian.dialog(requireContext(), "", "O App NÃO foi ativado, entre em contanto com suporte", {}, {}, "ok").show()
                ll_block.mshow()
            }
        }
    }

    private fun startListenerButton() {
        efab_start_listening.backgroundTintList = ContextCompat.getColorStateList(requireContext(), android.R.color.holo_green_dark)
        efab_start_listening.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_audio_on))
        tv_guardian_status.text = "GUARDIAN CAPTANDO..."
    }

    private fun stopListenerButton() {
        efab_start_listening.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
        efab_start_listening.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_audio_off))
        tv_guardian_status.text = "GUARDIAN PARADO"
        Helper.unmuteBeep()
    }


    // Cards Views Clicables
    override fun setupButtons() {

        efab_check_pay.setOnClickListener {
            Helper.checkPayment {
                Helper.checkPayment {
                    Helper.LogI("Pagamento encontrado $it...")
                    if (it) {
                        processPayment(true)
                    } else {
                        Guardian.getPrefDB()?.edit()?.putBoolean("app_status", false)?.apply()
                        Helper.LogI("Job App não ativado ...")
                    }
                }
            }
        }

        efab_start_listening.setOnClickListener {


            //AudioFileHelper.startRecordAudio(requireContext()) {
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
                            ended = false
                        }
                    }
                }
            } else {
                Guardian.dialog(requireContext(), "", "\n \nDeseja finalizar o Guardian?", {

                    stopListenerButton()
                    Helper.setListening(false)
                    Helper.stopService()
                    //GuardianSpeechListenerService.serviceOn = false
                    ended = true
                }, {
                    Helper.setListening(true)
                    startListenerButton()
                    //startListening() // keep listening
                }, "Finalizar", "não").show()


            }

        }
        fab_about_app.setOnClickListener {
            // AudioFileHelper.stopRecording()
            //  AudioFileHelper.playAudio(requireContext(), AudioFileHelper.currentAudio?.absolutePath)
            findNavController().navigate(R.id.action_goto_about_app)
        }

        fab_add_command.setOnClickListener {
            //AudioFileHelper.delete()
            // AudioFileHelper.playAudio(requireContext(), AudioFileHelper.currentAudio?.absolutePath)
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

                Guardian.dialog(requireContext(), "", "Permissão auto start", {
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

