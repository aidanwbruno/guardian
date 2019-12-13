package com.vdevcode.guardian.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.icu.lang.UProperty
import com.google.firebase.FirebaseApp
import com.vdevcode.guardian.helpers.Guardian
import com.vdevcode.guardian.helpers.Helper


class JobSchedulerRestartSpeechService : JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {
        Helper.LogW("LOTOS JOB RUNNING..")
        if (Guardian.online) {
            if (Helper.isListening() && !GuardianSpeechListenerService.serviceOn) {
                Helper.startService()
            }
        }
        return false // for complexly tasks use Thread like Coroutines or Asynctasks retur true otherwise return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

}