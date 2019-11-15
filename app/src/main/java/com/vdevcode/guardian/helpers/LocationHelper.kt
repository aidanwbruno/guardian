package com.vdevcode.guardian.helpers

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import com.vdevcode.guardian.models.UserLocation


object LocationHelper : LocationListener {

    var gpsEnable = false
    var netWorkEnable = false
    var myLocation = UserLocation()

    override fun onLocationChanged(location: Location?) {
        Helper.LogE("Location : ${location}")
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }

    override fun onProviderEnabled(p0: String?) {
    }

    override fun onProviderDisabled(p0: String?) {
    }

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
        manager.requestSingleUpdate(provider, this, null)
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
        if (!gpsOk(context)) {
            Guardian.dialog(context, "Ativar GPS", "Para lhe fornecer mais Segurança, o Guardian precisa acessar sua localização, Deseja Ativar?", {
                context.startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }, {}, "Ativar", "Não").show()
        }
    }


}