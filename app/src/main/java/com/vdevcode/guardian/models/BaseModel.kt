package com.vdevcode.guardian.models

import android.os.Parcelable
import androidx.room.Ignore
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.Parcelize
import java.util.*

abstract class BaseModel : Parcelable {

    var createdAt = Calendar.getInstance().timeInMillis// = Calendar.getInstance().timeInMillis
    var updatedAt = Calendar.getInstance().timeInMillis// = Calendar.getInstance().timeInMillis
    @Ignore
    @get:Exclude @set:Exclude @Exclude
    var collectionName = ""

    @Ignore
    @get:Exclude @set:Exclude @Exclude
    var firestoreKey = ""
}