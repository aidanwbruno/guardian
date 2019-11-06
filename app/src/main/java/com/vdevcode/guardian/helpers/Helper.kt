package com.vdevcode.guardian.helpers

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat


/**
 * Classe reponsável pelos metodos utilitarios para a aplicação.
 */
object Helper {

    private fun log(msg: String, type: Int) = when (type) {
        Log.ERROR -> Log.e(ConstantHelper.APP_LOG_TAG, msg)
        Log.INFO -> Log.i(ConstantHelper.APP_LOG_TAG, msg)
        Log.WARN -> Log.w(ConstantHelper.APP_LOG_TAG, msg)
        else -> Log.d(ConstantHelper.APP_LOG_TAG, msg)
    }

    fun LogE(msg: String) = log(msg, Log.ERROR)
    fun LogI(msg: String) = log(msg, Log.INFO)
    fun LogW(msg: String) = log(msg, Log.WARN)

}