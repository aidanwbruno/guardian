package com.vdevcode.guardian.models

import androidx.room.Entity
import androidx.room.Ignore
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
    @get:Exclude @set:Exclude @Exclude
    var userId: Int,
    var name: String,
    var email: String,
    var cpf: String,
    @get:Exclude @set:Exclude @Exclude
    var password: String, // only for login
    var nascimento: String,
    var complemento: String,
    var ativo: Boolean,

    // ======= endereço

    var cep: String,
    var rua: String,
    var numero: String,
    var bairro: String,
    var cidade: String,
    var estado: String,
    var motorista: Boolean?,
    @Ignore
    var carros: MutableMap<String, Car>? = mutableMapOf()

) : BaseModel() {

    //@Ignore
    // var LOCATIONS: MutableMap<String, String>? = mutableMapOf()
    var ultimaLocalizacao: String

    constructor() : this(0, "", "", "", "", "", "", true, "", "", "", "", "", "", null, mutableMapOf()) {}

    init {
        collectionName = ConstantHelper.FIREBASE_USER_COLLECTION_NAME
        ultimaLocalizacao = ""
    }


}