package com.vdevcode.guardian.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.icu.lang.UProperty
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.vdevcode.guardian.auth.AppAuth
import com.vdevcode.guardian.database.AppFireDB
import com.vdevcode.guardian.extensions.mhide
import com.vdevcode.guardian.extensions.mshow
import com.vdevcode.guardian.helpers.Guardian
import com.vdevcode.guardian.helpers.Helper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class JobSchedulerRestartSpeechService : JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {
        Helper.LogW("LOTOS JOB RUNNING..")
        if (Guardian.online) {
            if (Helper.isListening() && !GuardianSpeechListenerService.serviceOn) {
                Helper.startService()
            }
        }

        //Helper.showServiceNotification(this, 2, "App Guadian Ativado T", " T Seu Aplicativo guardian foi ativado com sucesso!", true)

        val appStatus = false
        val appPayment = Guardian.getPrefDB()?.getBoolean("app_payment", false)
        var statusCount = Guardian.getPrefDB()?.getInt("app_status_count", 0) ?: 0
        if (appStatus == false) {
            AppFireDB.checkActivation(AppAuth.getUserId()) {
                Helper.LogI("User Ativo no firebase $it")
                if (it) {
                    Guardian.getPrefDB()?.edit()?.putBoolean("app_status", true)?.apply()
                    if (statusCount == 0) {
                        Guardian.getPrefDB()?.edit()?.putInt("app_status_count", 10)?.apply()
                        Helper.showServiceNotification(this, 2, "App Guadian Ativado", "Seu Aplicativo guardian foi ativado com sucesso!", true)
                    }
                } else {
                    if (appPayment == false) {
                        Helper.checkPayment {
                            Helper.LogI("Pagamento encontrado $it...")
                            if (it) {
                                GlobalScope.launch(Dispatchers.Main) {
                                    checkAppFirebase()
                                }
                            } else {
                                Guardian.getPrefDB()?.edit()?.putBoolean("app_status", false)?.apply()
                                Guardian.getPrefDB()?.edit()?.putBoolean("app_payment", false)?.apply()
                                Helper.LogI("Job App não ativado ...")
                            }
                        }
                    } else {
                        checkAppFirebase()
                    }
                }
            }
        }

        return false // for complexly tasks use Thread like Coroutines or Asynctasks retur true otherwise return false
    }

    private fun checkAppFirebase() {
        Guardian.getPrefDB()?.edit()?.putBoolean("app_payment", true)?.apply()
        AppFireDB.activateApp(AppAuth.getUserId()) {
            Helper.LogI("JOB Usuário foi ativado no Firebase: $it...")
            if (it) {
                Guardian.getPrefDB()?.edit()?.putBoolean("app_status", true)?.apply();
                Helper.showServiceNotification(this, 2, "App Guadian Ativado", "Seu Aplicativo guardian foi ativado com sucesso!", true)
            } else {
                Guardian.getPrefDB()?.edit()?.putBoolean("app_status", false)?.apply()
                Helper.showServiceNotification(this, 2, "App Guadian Erro", "Pagamento encontrado, mas ocorreu um erro ao ativar o app!", true)
            }
        }
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

}