package com.vdevcode.guardian.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.vdevcode.guardian.helpers.ConstantHelper
import kotlinx.android.parcel.Parcelize

/**
 *
 * Room and Firebase classe, to store user data
 */

@Parcelize
@Entity(tableName = "users")
@IgnoreExtraProperties
class AppUser(

    @PrimaryKey(autoGenerate = true)
    @get:Exclude
    var userId: Int,
    var name: String,
    var email: String,
    var cpf: String,
    @get:Exclude
    var password: String, // only for login
    var nascimento: String,

    // ======= endereço

    var cep: String,
    var rua: String,
    var numero: String,
    var bairro: String,
    var cidade: String,
    var estado: String,

    var motorista: Boolean?,
    var carro: Boolean?,
    var marca: String?,
    var modelo: String?,
    var cor: String?
) : BaseModel() {

    constructor() : this(0, "", "", "", "", "", "", "", "", "", "", "", null, null, null, null, null) {}

    init {
        collectionName = ConstantHelper.FIREBASE_USER_COLLECTION_NAME
    }


}