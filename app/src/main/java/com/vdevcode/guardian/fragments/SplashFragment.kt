package com.vdevcode.guardian.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.vdevcode.guardian.R
import com.vdevcode.guardian.auth.AppAuth
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 */
class SplashFragment : BaseFragment(R.layout.fragment_splash, "Login", false, null) {

    override fun buildFragment() {
    }

    override fun homeIconClicked() {
    }

    override fun onResume() {
        super.onResume()
        GlobalScope.launch {
            delay(2000)
            if (AppAuth.isUserLogged()) {
                findNavController().navigate(R.id.action_global_main)
            } else {
                findNavController().navigate(R.id.action_goto_login)
            }
        }
    }
}
