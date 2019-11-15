package com.vdevcode.guardian.helpers

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.vdevcode.guardian.models.UserLocation
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.Task
import android.content.IntentSender.SendIntentException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.rpc.Help
import android.widget.Toast
import androidx.annotation.NonNull
import com.google.android.gms.tasks.OnCompleteListener
import com.google.gson.Gson
import com.vdevcode.guardian.auth.AppAuth
import com.vdevcode.guardian.database.AppFireDB
import com.vdevcode.guardian.models.Alert
import java.util.*


const val LOCATION_UPADATE_INTERVAL = 60000L // 10 secounds
const val FAST_LOCATION_UPADATE_INTERVAL = 30000L // 5 secounds
const val REQUEST_CHECK_SETTINGS = 383 // 5 secounds

class GoogleLocationHelper {


    var fusedLocationClient: FusedLocationProviderClient? = null
    var locationSettingsClient: SettingsClient? = null
    var locationSettingsReq: LocationSettingsRequest? = null
    var locationRequest: LocationRequest? = null
    var locationCallback: LocationCallback? = null
    var currentLocation: UserLocation? = null


    fun init(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationSettingsClient = LocationServices.getSettingsClient(context)

        addLocationCallback()

        locationRequest = LocationRequest()
        locationRequest?.apply {
            interval = LOCATION_UPADATE_INTERVAL
            fastestInterval = FAST_LOCATION_UPADATE_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationSettingsReq = LocationSettingsRequest.Builder().addLocationRequest(locationRequest!!).build()
    }


    fun startLocationUpdates(context: Activity) {
        locationSettingsClient?.let {
            val task = it.checkLocationSettings(locationSettingsReq)
            task.addOnCompleteListener {
                try {
                    // location updates
                    val response = it.getResult(ApiException::class.java)
                    fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
                } catch (apiExe: ApiException) {
                    when (apiExe.statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            resolveLocationError(context, apiExe)
                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> checkUserGPS(context)
                    }
                }
            }
        }
    }

    fun checkFixResult(reqCode: Int, resCode: Int) {
        when (reqCode) {
            REQUEST_CHECK_SETTINGS -> {
                when (resCode) {
                    Activity.RESULT_OK -> Helper.LogI("GPS Settings result ok")
                    Activity.RESULT_CANCELED -> Helper.LogI("User Denied the fix")
                }
            }
        }
    }

    private fun resolveLocationError(context: Activity, exception: ApiException) {
        try {
            // Cast to a resolvable exception.
            val resolvable = exception as ResolvableApiException
            // Show the dialog by calling startResolutionForResult(),
            // and check the result in onActivityResult().
            resolvable.startResolutionForResult(context, REQUEST_CHECK_SETTINGS)
        } catch (e: SendIntentException) {
            // Ignore the error.
        } catch (e: ClassCastException) {
            checkUserGPS(context)
        }

    }


    private fun addLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                super.onLocationResult(result)
                result?.lastLocation?.let {
                    currentLocation = UserLocation().apply {
                        longitude = it.longitude
                        latitude = it.latitude
                    }
                    val locationString = currentLocation?.toJson()
                    locationString?.let { loc ->
                        AppFireDB.currentAlert?.let { alert ->
                            AppFireDB.findDocumentById(alert).get().addOnCompleteListener { ref ->
                                if (ref.isSuccessful) {
                                    val alertDoc = ref.result?.toObject(Alert::class.java)
                                    if (alertDoc != null) {
                                        if (alertDoc.open) {
                                            // update user location
                                            AppFireDB.findUserById(AppAuth.getUserId()).collection(ConstantHelper.FIREBASE_USER_LOCATIONS_COLLECTION_NAME).document(Calendar.getInstance().timeInMillis.toString()).set(mapOf("point" to loc)).addOnCompleteListener {
                                                if (it.isSuccessful) {
                                                    Helper.LogW("Location added to user")
                                                    Helper.LogW("Location :  LAT: ${currentLocation?.latitude} , LNG: ${currentLocation?.longitude}")
                                                    Guardian.toast("Location (${currentLocation?.latitude}, ${currentLocation?.longitude})")
                                                }
                                            }
                                        } else {
                                            AppFireDB.currentAlert = null // alert was closed
                                        }
                                    } else {
                                        AppFireDB.currentAlert = null // alert was closed
                                    }
                                } else {
                                    AppFireDB.currentAlert = null // alert was closed
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    var gpsEnable = false
    var netWorkEnable = false
    var myLocation = UserLocation()


    @SuppressLint("MissingPermission")
    fun getLocationManager(context: Context) = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager


    fun getNetworkLocation(context: Context): UserLocation? {
        val manager = getLocationManager(context)
        netWorkEnable = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (netWorkEnable) {
            return getSingleUpdate(manager, LocationManager.NETWORK_PROVIDER)
        }
        return null
    }


    fun getGpsLocation(context: Context): UserLocation? {
        val manager = getLocationManager(context)
        gpsEnable = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (gpsEnable) {
            return getSingleUpdate(manager, LocationManager.GPS_PROVIDER)
        }
        return null
    }

    fun gpsOk(context: Context): Boolean {
        val manager = getLocationManager(context)
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun getSingleUpdate(manager: LocationManager, provider: String): UserLocation? {
        // manager.requestSingleUpdate(provider, this, null)
        val location = manager.getLastKnownLocation(provider);
        if (location != null) {
            return myLocation.apply {
                latitude = location.latitude
                longitude = location.longitude
            }
        }
        return null
    }

    fun checkUserGPS(context: Context) {
        //if (!gpsOk(context)) {
        Guardian.dialog(context, "Ativar GPS", "Para lhe fornecer mais Segurança, o Guardian precisa acessar sua localização, Deseja Ativar?", {
            context.startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }, {}, "Ativar", "Não").show()
        //  }
    }


    fun stopLocationListener() {
        fusedLocationClient?.run {
            locationCallback?.let {
                removeLocationUpdates(it)
                    .addOnCompleteListener {
                        Helper.LogW("Parando de receber localizações")
                    }

            }
        }
    }


}