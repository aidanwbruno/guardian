package com.vdevcode.guardian.helpers

import android.content.ComponentName
import android.content.Intent
import android.net.Uri


object ConstantHelper {

    const val RESTART_SERVICE_JOB_ID = 354

    val APP_PERMISSION_REQ_CODE = 2


    val USER_PROFILE_NAME_MANAGER = "Gerente"
    /**
     * Usuários estoquistas, responsáveis por pegar os produtos no estoque
     */
    val USER_PROFILE_TYPE_STORAGE = 1
    val USER_PROFILE_NAME_STORAGE = "Estoquista"
    /**
     * usuarios repositores de produtos
     */
    val USER_PROFILE_TYPE_REPOSITOR = 0
    val USER_PROFILE_NAME_REPOSITOR = "Repositor"

    /**
     * Paramentro usado para enviar o perfil do usuário, via bundle or similares
     */
    val USER_PROFILE_TYPE_PARAM_KEY = "user_profile_type"


    /**
     * Tag para logs do Applicativo
     */
    val APP_LOG_TAG = "GUARDIAN_TAG"

    //=======================================  FIREBASE CONSTANTS ========================================================

    val FIREBASE_USER_COLLECTION_NAME = "USUARIOS"
    val FIREBASE_ALERT_COLLECTION_NAME = "ALERTAS"
    val FIREBASE_USER_LOCATIONS_COLLECTION_NAME = "LOCATIONS"


    val AUTO_START_INTENTS = arrayOf(
        Intent().setComponent(
            ComponentName(
                "com.samsung.android.lool",
                "com.samsung.android.sm.ui.battery.BatteryActivity"
            )
        ), Intent("miui.intent.action.OP_AUTO_START").addCategory(Intent.CATEGORY_DEFAULT), Intent().setComponent(
            ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"
            )
        ), Intent().setComponent(
            ComponentName(
                "com.letv.android.letvsafe",
                "com.letv.android.letvsafe.AutobootManageActivity"
            )
        ), Intent().setComponent(
            ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.optimize.process.ProtectActivity"
            )
        ), Intent().setComponent(
            ComponentName(
                "com.coloros.safecenter",
                "com.coloros.safecenter.permission.startup.StartupAppListActivity"
            )
        ), Intent().setComponent(
            ComponentName(
                "com.coloros.safecenter",
                "com.coloros.safecenter.startupapp.StartupAppListActivity"
            )
        ), Intent().setComponent(
            ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")
        ), Intent().setComponent(
            ComponentName(
                "com.iqoo.secure",
                "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
            )
        ), Intent().setComponent(
            ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")
        ), Intent().setComponent(
            ComponentName(
                "com.vivo.permissionmanager",
                "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
            )
        ), Intent().setComponent(
            ComponentName(
                "com.asus.mobilemanager",
                "com.asus.mobilemanager.entry.FunctionActivity"
            )
        ).setData(
            Uri.parse("mobilemanager://function/entry/AutoStart")
        )
    )

    //==================================================== PREFERENCES DB ============================================================
    const val PREFERENCE_SESSION_DB = "GUARDIAN_PREF_DB"
    const val APP_STATUS_LISTENING = "SPEECH_MODE_ON"
    //==================================================== Models =============================

    val WORD_GROUP_CUSTOM = 2


}