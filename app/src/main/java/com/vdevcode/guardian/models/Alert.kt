package com.vdevcode.guardian.models

import com.google.firebase.firestore.IgnoreExtraProperties
import com.vdevcode.guardian.auth.AppAuth
import com.vdevcode.guardian.helpers.ConstantHelper
import kotlinx.android.parcel.Parcelize

/**
 *
 * Room and Firebase classe, to store user data
 */

@Parcelize
@IgnoreExtraProperties
class Alert(var usuarioKey: String) : BaseModel() {

    var open: Boolean
    var count: Int

    constructor() : this(AppAuth.getUserId()) {}

    init {
        open = false
        count = 0
        collectionName = ConstantHelper.FIREBASE_ALERT_COLLECTION_NAME
    }

}