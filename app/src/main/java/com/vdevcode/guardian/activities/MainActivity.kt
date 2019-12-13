package com.vdevcode.guardian.activities

import android.Manifest
import android.app.AlarmManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.vdevcode.guardian.R
import com.vdevcode.guardian.helpers.ConstantHelper
import com.vdevcode.guardian.helpers.GoogleLocationHelper
import com.vdevcode.guardian.helpers.Guardian
import com.vdevcode.guardian.helpers.Helper
import com.vdevcode.guardian.services.JobSchedulerRestartSpeechService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val googleLocationHelper = GoogleLocationHelper()
    private var gps: Boolean? = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //if (!Vocc.online) {
        Helper.regiterInternetCheck(this)
        //}
        startJobScheduler()
        //registerReceiver( rec, IntentFilter(AudioManager.ACTION_MICROPHONE_MUTE_CHANGED))
        // FacebookSdk.sdkin

        //LocationHelper.checkUserGPS(context!!)

        googleLocationHelper.init(this)
        googleLocationHelper.startLocationUpdates(this) // get the locations
        sw_location.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                // Ativar GPS
                //googleLocationHelper.init(this)
                gps = true
                Guardian.toast("Ativando Localização")
                googleLocationHelper.startLocationUpdates(this) // get the locations
            } else {
                // desativar GPS
                //googleLocationHelper.init(this)
                gps = false
                Guardian.toast("Desativando caputa de Localização")
                googleLocationHelper.stopLocationListener()
            }
        }

    }


    override fun onResume() {
        super.onResume()
        gps?.let {
            gps = googleLocationHelper.gpsOk(this)
            sw_location.isChecked = it
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        gps = googleLocationHelper.checkFixResult(requestCode, resultCode)
        sw_location.isChecked = gps ?: false
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            ConstantHelper.APP_PERMISSION_REQ_CODE -> {
                for (p in 0..permissions.size - 1) {
                    if (permissions[p] == Manifest.permission.ACCESS_FINE_LOCATION && grantResults[p] == PackageManager.PERMISSION_GRANTED) {
                        //val location = LocationHelper.getGpsLocation(context!!)
                        googleLocationHelper.startLocationUpdates(this)
                    } else {
                        Guardian.requestSinglePermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
            }
        }
    }

    private fun startJobScheduler() {
        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        if (!checkIfJobExists(jobScheduler)) {
            val componentName = ComponentName(packageName, JobSchedulerRestartSpeechService::class.java.name)
            val jobBuilder = JobInfo.Builder(ConstantHelper.RESTART_SERVICE_JOB_ID, componentName)
                .setPeriodic(AlarmManager.INTERVAL_FIFTEEN_MINUTES)
                .setPersisted(true).build()
            if (jobScheduler.schedule(jobBuilder) > 0) {
                Helper.LogW("GUARDIAN JOB STATED")
            }
        } else {
            Helper.LogW("GUARDIAN JOB ALREADY IS RUNNING...")
        }
    }

    private fun checkIfJobExists(jobScheduler: JobScheduler): Boolean {
        jobScheduler.allPendingJobs.forEach { item ->
            return item.id == ConstantHelper.RESTART_SERVICE_JOB_ID
        }
        return false
    }
}
