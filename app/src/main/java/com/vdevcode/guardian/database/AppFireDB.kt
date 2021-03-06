package com.vdevcode.guardian.database


import android.renderscript.Sampler
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.vdevcode.guardian.auth.AppAuth
import com.vdevcode.guardian.helpers.ConstantHelper
import com.vdevcode.guardian.helpers.Helper
import com.vdevcode.guardian.models.Alert
import com.vdevcode.guardian.models.AppUser
import com.vdevcode.guardian.models.BaseModel
import com.vdevcode.guardion.helpers.AudioFileHelper
import com.vdevcode.guardion.helpers.FileHelper

/**
 * Class used  to perform Firestore operations
 * @author Miguel Vieira
 */
object AppFireDB {

    var currentAlert: Alert? = null

    fun updateCurrentAlert(audioUrl: String?) {
        currentAlert?.let { alertDoc ->
            if (alertDoc.open) {
                findDocumentById(alertDoc).get().addOnCompleteListener { ref ->
                    if (ref.isSuccessful) {
                        val alert = ref.result?.toObject(Alert::class.java)
                        alert?.let {
                            alert.firestoreKey = ref.result?.id!!
                            if (alert.open) {
                                val params = mutableMapOf<String, Any>()
                                params.put("count", FieldValue.increment(1))
                                if (AudioFileHelper.isFileOk()) {
                                    audioUrl?.let {
                                        Helper.LogI("Audio OK")
                                        params.put("audio", FileHelper.getFileName(it))
                                    }
                                }
                                DB().collection(alertDoc.collectionName).document(alertDoc.firestoreKey).update(params).addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        Helper.LogI("Alerta Atualizado")
                                        if (AudioFileHelper.isFileOk()) {
                                            AudioFileHelper.delete()
                                        }
                                    } else {
                                        if (it.exception != null) {
                                            Helper.LogI("Erro ao atualizar alerta: ${it.exception?.message}")
                                        }
                                    }
                                }
                            } else {
                                currentAlert = null // alert was closed
                            }
                        }
                    }
                }
            }
        }
    }

    fun insertModel(model: BaseModel) {
        DB().collection(model.collectionName).add(model)
    }

    fun insertModel(model: BaseModel, completeListener: OnCompleteListener<DocumentReference>) {
        DB().collection(model.collectionName)
            .add(model)
            .addOnCompleteListener(completeListener)
    }

    fun insertOrReplaceModelById(model: BaseModel, completeListener: OnCompleteListener<Void>) {
        DB().collection(model.collectionName)
            .document(model.firestoreKey)
            .set(model)
            .addOnCompleteListener(completeListener)
    }

    fun tryCreateNewModel(model: BaseModel, vararg callback: () -> Any?) {
        findDocumentById(model).get().addOnSuccessListener { doc ->
            if (doc == null || !doc.exists()) {
                insertOrReplaceModelById(model, OnCompleteListener { res ->
                    if (res.isSuccessful) {
                        callback[0].invoke()
                        //MyApp.toast("Dados Cadastrados com Sucesso :) !")
                        //MyApp.navigateTo(R.id.action_login_success, true, null)
                        // todo put accout status here
                    } else {
                        res.exception?.run {
                            AppAuth.showFirebaseException(this)
                        }
                    }
                })
            } else {
                // user already created
                //PromoApp.navigateTo(R.id.action_login_success, true, null)
            }
        }.addOnFailureListener {
            AppAuth.showFirebaseException(it)
            callback?.get(1)?.invoke()
        }
    }


    fun findDocumentById(model: BaseModel): DocumentReference {
        return DB().collection(model.collectionName).document(model.firestoreKey)
    }

    fun findUserById(key: String): DocumentReference {
        return DB().collection(ConstantHelper.FIREBASE_USER_COLLECTION_NAME).document(key)
    }


    fun checkActivation(emailKey: String, onComplete: (ok: Boolean) -> Unit) {
        if (AppAuth.getUserId().isNullOrBlank()) {
            onComplete.invoke(false)
            return
        }
        findUserById(emailKey).get().addOnCompleteListener {
            if (it.isSuccessful) {
                val user = it.result.toObject(AppUser::class.java)
                user?.let {
                    onComplete.invoke(it.ativo)
                    return@addOnCompleteListener
                }
            }
            onComplete.invoke(false)
        }
    }


    fun activateApp(emailKey: String, onComplete: (ok: Boolean) -> Unit) {
        findUserById(emailKey).update("ativo", true).addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete.invoke(true)
                return@addOnCompleteListener
            }
            onComplete.invoke(false)
        }
    }

    fun deleteDocument(model: BaseModel) {
        DB().collection(model.collectionName).document(model.firestoreKey).delete()
    }

    fun deleteDocument(collection: String, doc: String) {
        DB().collection(collection).document(doc).delete()
    }

    fun deleteDocument(model: BaseModel, completeListener: OnCompleteListener<Void>) {
        DB().collection(model.collectionName).document(model.firestoreKey).delete().addOnCompleteListener(completeListener)
    }

    fun query(collectionName: String, field: String, operator: String, value: Any) = DB().collection(collectionName).whereEqualTo(field, value)
    //.whereLessThanOrEqualTo(field, value)

    fun list(collectionName: String) = DB().collection(collectionName)

    /**
     * Get the Firestore Database Instance
     */
    fun DB() = FirebaseFirestore.getInstance()

}

