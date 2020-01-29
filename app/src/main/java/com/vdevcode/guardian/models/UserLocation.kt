package com.vdevcode.guardian.models


import com.google.gson.Gson
import java.util.*


class UserLocation() {
    var longitude: Double
    var latitude: Double
    var createdAt: Long

    init {
        longitude = 0.0
        latitude = 0.0
        createdAt = Calendar.getInstance().timeInMillis
    }

    fun toJson() = Gson().toJson(this)
}