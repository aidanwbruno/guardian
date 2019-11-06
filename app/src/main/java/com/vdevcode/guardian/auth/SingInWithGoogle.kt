package com.vdev.promo.Auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
/*
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.vdev.promo.R

@Suppress("CAST_NEVER_SUCCEEDS")
class SingInWithGoogle(var context: Context) {

    private var googleClient: GoogleSignInClient
    var fragment: Fragment? = null

    init {
        googleClient = GoogleSignIn.getClient(context, loginRequest())
    }

    constructor(context: Context, fragment: Fragment) : this(context) {
        this.fragment = fragment
    }

    companion object {
        val REQUEST_CODE = 9001
    }

    fun loginRequest(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }

    // step 1
    fun singIn() {
        //if (Liso.isOnline) {
        val intent = googleClient.getSignInIntent()
        //LisoUtils.showAboutApp(ac, true)

        fragment?.startActivityForResult(intent, REQUEST_CODE) ?: (context as Activity).run {
            startActivityForResult(
                intent,
                REQUEST_CODE
            )
        }

        //} else {
        // Liso.message(ac, ac.getString(R.string.connection_message))
        //}
    }

    //step 2
    fun firebaseSingIn(data: Intent?) {
        val account = getAccount(data)
        if (account != null) {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            AppAuth.singInWithCredential(credential)
        }
    }

    fun linkToGoogle(data: Intent?) {
        val account = getAccount(data)
        if (account != null) {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            //LisoAuth.linkAccount(credential)
        }
    }

    fun signOut() {
        googleClient.signOut().addOnCompleteListener {
            if (it.isSuccessful) {
                println("Sing out Google ok")
            }
        }
    }

    // Login que gooogle ja seta o display name do usuario
    private fun createUserDoc() {
        /*val fUser = LisoAuth.currentUser()
        val userDoc = User(fUser!!.uid, fUser.displayName!!, fUser.email!!, "")
        UserController.createUserIfNecessary(userDoc) // add  USER DOCUMENT TO FIRE STORE
        */
    }

    private fun getAccount(data: Intent?): GoogleSignInAccount? {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        var accont: GoogleSignInAccount? = null
        try {
            accont = task.getResult(ApiException::class.java)
        } catch (ex: ApiException) {
            println("Error ao logar com google")
            ex.printStackTrace()
            return null
        }
        return accont
    }

}

 */