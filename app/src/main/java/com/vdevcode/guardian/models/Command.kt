package com.vdevcode.guardian.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "commands")
class Command : BaseModel() {
    @PrimaryKey(autoGenerate = true)
    var comandoId: Long
    var palavra: String

    init {
        comandoId = 0
        palavra = ""
    }
}