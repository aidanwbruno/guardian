package com.vdevcode.guardian.database


import android.renderscript.Sampler
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.vdevcode.guardian.auth.AppAuth
import com.vdevcode.guardian.models.BaseModel

/**
 * Class used  to perform Firestore operations
 * @author Miguel Vieira
 */
object AppFireDB {

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

