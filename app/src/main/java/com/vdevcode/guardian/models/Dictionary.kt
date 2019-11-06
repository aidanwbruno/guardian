package com.vdevcode.guardian.models

import kotlinx.android.parcel.Parcelize

// on firebase this class will be  aggregated
@Parcelize
class Dictionary : BaseModel() {

    var user: AppUser?
    var command: Command?

    init {
        user = null
        command = null
    }
}