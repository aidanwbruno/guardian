package com.vdevcode.guardian.activities

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.vdevcode.guardian.R
import com.vdevcode.guardian.extensions.mhide
import com.vdevcode.guardian.helpers.ConstantHelper
import com.vdevcode.guardian.helpers.GoogleLocationHelper
import com.vdevcode.guardian.helpers.Guardian
import com.vdevcode.guardian.helpers.Helper
import com.vdevcode.guardian.services.JobSchedulerRestartSpeechService
import kotlinx.android.synthetic.main.activity_main.*

//vdev.code@guardian@2020
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

        ll_gps_check.mhide()
        googleLocationHelper.init(this)
        googleLocationHelper.startLocationUpdates(this) // get the locations

        gps?.let {
            // if (googleLocationHelper.locationOk == null && currentFocus?.id == R.id.main_frag ) {
            // sw_location.isChecked = true
            //  googleLocationHelper.startLocationUpdates(this) // get the locations
            // } else {
            gps = googleLocationHelper.gpsOk(this)
            sw_location.isChecked = it
            //}
        }

        sw_location.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                // Ativar GPS
                //googleLocationHelper.init(this)
                if (googleLocationHelper.locationOk == null || googleLocationHelper.locationOk == false) {
                    gps = true
                    Guardian.toast("Ativando Localiza????o")
                    googleLocationHelper.startLocationUpdates(this) // get the locations
                }
            } else {
                if (googleLocationHelper.locationOk == true) {
                    // desativar GPS
                    //googleLocationHelper.init(this)
                    gps = false
                    Guardian.toast("Desativando captura de Localiza????o")
                    googleLocationHelper.stopLocationListener()
                }
            }
        }

        // Creates instance of the manager.
        checkUpdate()
    }

    private fun checkUpdate() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        // Checks that the platform will allow the specified type of update.
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                appUpdateManager.startUpdateFlowForResult(
                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
                    appUpdateInfo,
                    // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                    AppUpdateType.IMMEDIATE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    12
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        gps = googleLocationHelper.checkFixResult(requestCode, resultCode)
        sw_location.isChecked = gps ?: false

        if (requestCode == 12 && resultCode == Activity.RESULT_OK) {
            Guardian.toast("App Atualizado com sucesso")
        }

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
