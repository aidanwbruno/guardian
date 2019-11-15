package com.vdevcode.guardian.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

// on firebase this class will be  aggregated
@Parcelize
class Car(
    var marca: String?,
    var modelo: String?,
    var cor: String?,
    var placa: String?
) : Parcelable {
    constructor() : this("", "", "", "")
}