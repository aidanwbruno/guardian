package com.vdevcode.guardian.helpers

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.view.Gravity
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat.startActivity
import android.content.Intent
import android.content.ComponentName
import android.content.DialogInterface
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar


/**
 * Classe reponsável pelos metodos usados especialmente para interação direta com UI e Contextos. Esta classe armazena o Contexto da aplicação.
 * Funciona como um scopo de sesssão, estando disponivel durante o cliclo de vida da aplicação
 */
class Guardian : Application() {


    companion object {
        var appContext: Context? = null

        /**
         * Exibe um toast na tela do usuário com uma mensagem de texto. Em poucos segundos a mesnagem some da tela.
         */
        fun toast(msg: String) {
            Toast.makeText(appContext, msg, Toast.LENGTH_SHORT).apply {
                setGravity(Gravity.TOP, 0, 0)
            }.show()
        }

        fun snack(view: View, msg: String) {
            Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show()
        }

        fun snack(view: View, msg: String, time: Int) {
            Snackbar.make(view, msg, time).show()
        }

        fun checkPemission(context: Context, permission: String) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // mMap.setMyLocationEnabled(true);
            } else {
                // Show rationale and request permission.
            }
        }


        @RequiresApi(api = Build.VERSION_CODES.M)
        fun requestAppPermissions(myActivity: Fragment?, vararg permissions: String) {
            myActivity?.let { ctx ->
                val notGranted = arrayListOf<String>()
                permissions.forEach { permission ->
                    if (ContextCompat.checkSelfPermission(ctx.context!!, permission) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notGranted.add(permission)
                    }
                }
                if (notGranted.isNotEmpty()) {
                    myActivity.requestPermissions(
                        notGranted.toArray(arrayOf()),
                        ConstantHelper.APP_PERMISSION_REQ_CODE
                    )
                }
            }
        }


        @RequiresApi(api = Build.VERSION_CODES.M)
        fun requestSinglePermission(context: Activity, vararg permission: String) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission[0]
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                context.requestPermissions(permission, ConstantHelper.APP_PERMISSION_REQ_CODE)
            }
        }


        fun dialog(context: Context, title: String, msg: String) = MaterialAlertDialogBuilder(context).apply {
            setTitle(title)
            setMessage(msg)
        }

        fun dialog(context: Context, title: String, msg: String, okCallback: (DialogInterface) -> Unit, cancelCallback: () -> Unit, vararg buttons: String?) = MaterialAlertDialogBuilder(context).apply {
            setTitle(title)
            setMessage(msg)
            if (!buttons.isNullOrEmpty()) {
                if (buttons.size == 1) {
                    setNegativeButton(buttons[0]) { dialogInterface, i ->
                        cancelCallback.invoke()
                        dialogInterface.dismiss()
                    }
                } else
                    if (buttons.size == 2) {
                        setNegativeButton(buttons[1]) { dialogInterface, i ->
                            cancelCallback.invoke()
                            dialogInterface.dismiss()
                        }
                        setPositiveButton(buttons[0]) { dialogInterface, i ->
                            okCallback.invoke(dialogInterface)
                        }
                    }
            }
            buttons.forEach {


            }
        }.create()


        fun openVoiceSettings() {
            try {
                val vsInt = Intent(Intent.ACTION_MAIN)
                vsInt.component = ComponentName(
                    "com.google.android.voicesearch",
                    "com.google.android.voicesearch.VoiceSearchPreferences"
                )
                vsInt.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                appContext?.startActivity(vsInt)

            } catch (e: Exception) {

                try {
                    val vsjInt = Intent(Intent.ACTION_MAIN)
                    vsjInt.component = ComponentName("com.google.android.googlequicksearchbox", "com.google.android.voicesearch.VoiceSearchPreferences")
                    vsjInt.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    appContext?.startActivity(vsjInt)

                } catch (e1: Exception) {
                    e1.printStackTrace()
                }

            }

        }

        fun putPrefString(key: String, value: String) {
            val prefDB = appContext?.getSharedPreferences(ConstantHelper.PREFERENCE_SESSION_DB, Context.MODE_PRIVATE)
            prefDB?.edit()?.putString(key, value)?.apply()
        }

        fun getPrefDB() = appContext?.getSharedPreferences(ConstantHelper.PREFERENCE_SESSION_DB, Context.MODE_PRIVATE)


    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }

}