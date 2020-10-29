package com.vdevcode.guardian.auth

import com.google.firebase.auth.*
import com.vdevcode.guardian.database.AppFireDB
import com.vdevcode.guardian.helpers.Guardian
import com.vdevcode.guardian.helpers.Helper
import com.vdevcode.guardian.models.AppUser

object AppAuth {

    fun getAuth() = FirebaseAuth.getInstance()

    fun createUserAccount(user: AppUser, success: () -> Unit) {
        getAuth().createUserWithEmailAndPassword(user.email, user.password).addOnCompleteListener {
            if (it.isSuccessful) {
                user.firestoreKey = getUserId()
                AppFireDB.tryCreateNewModel(user, success) // add  USER DOCUMENT TO FIRE STORE
                Guardian.getPrefDB()?.edit()?.putBoolean("app_status", false)?.apply();
            } else {
                showFirebaseException(it.exception)
            }
        }
    }


    /**
     * Realixa login com firebase, usando um email e password. O Usuario deve ter sua conta criada no firebase authentication
     */
    fun singInWithEmailAndPassword(email: String, pass: String, callback: () -> Any?) {
        getAuth().signInWithEmailAndPassword(email, pass).addOnCompleteListener {
            if (it.isSuccessful) {
                callback.invoke()
                //create the user if he doesn't exists on
                val user = it.result?.user
                /*AppFireDB.tryCreateNewModel(getFirebaseUserData(user!!).apply {
                    firestoreKey = user.uid // id for document
                }, {})

                 */
                Helper.LogI("Login OK")
            } else {
                showFirebaseException(it.exception)
            }
        }
    }


    fun singInWithCredential(crendentials: AuthCredential) {
        getAuth().signInWithCredential(crendentials).addOnCompleteListener { result ->
            if (result.isSuccessful) {
                result.result?.let { data ->
                    data.user?.let { user ->
                        //create the user if he doesn't exists on
                        AppFireDB.tryCreateNewModel(getFirebaseUserData(user).apply {
                            firestoreKey = user.uid // id for document
                        }, {})
                    }
                }
                Helper.LogI("USER LOGGER WITG GOOGLE ACCOUNT : " + result.result?.user?.displayName)
            } else {
                showFirebaseException(result.exception)
            }
        }
    }

    fun showFirebaseException(ex: Exception?) {
        println("$ex - ${ex?.message}")
        when (ex) {
            is FirebaseAuthRecentLoginRequiredException -> Guardian.toast("Reautenticação necessária!")
            is FirebaseAuthInvalidCredentialsException -> Guardian.toast("Ops! Email ou Senha inválidos!")
            is FirebaseAuthUserCollisionException -> Guardian.toast("Ops! Email ou Usuário já Cadastrado!")
            is FirebaseAuthWeakPasswordException -> Guardian.toast("Ops! A senha deve ter 6 caracteres!")
            is FirebaseAuthInvalidUserException -> Guardian.toast("Ops! Usuário não encontrado!")
            is FirebaseAuthActionCodeException -> Guardian.toast("Ops! Código de Ação!")
            is FirebaseAuthProvider -> Guardian.toast("Erro: provider!")
            else -> Guardian.toast("Ops! algum probleminha aconteçeu!")
        }
        //LisoUtils.dialog?.dismiss()
    }


    fun getFirebaseUserData(user: FirebaseUser): AppUser {
        val userModel = AppUser()
        with(userModel) {
            user.displayName?.let {
                name = it
            }
            user.email?.let {
                email = it
            }
        }
        return userModel
    }


    fun getUser(): FirebaseUser? {
        return getAuth().currentUser
    }

    fun getUserId(): String {
        getUser()?.let {
            it?.email?.let {
                if (it.isNotBlank()) {
                    return it
                }
            }
            return it.uid
        }
        return ""
    }

    fun isUserLogged(): Boolean {
        return getUser() != null
    }


}