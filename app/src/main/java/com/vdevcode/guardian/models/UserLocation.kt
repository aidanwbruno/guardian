package com.vdevcode.guardian.models


import com.google.gson.Gson


class UserLocation() {
    var longitude: Double
    var latitude: Double

    init {
        longitude = 0.0
        latitude = 0.0
    }

    fun toJson() = Gson().toJson(this)
}