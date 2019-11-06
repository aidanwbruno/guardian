package com.vdevcode.guardian.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.vdevcode.guardian.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 */
class SplashFragment : BaseFragment(R.layout.fragment_splash, "Login") {

    override fun buildFragment() {
        GlobalScope.launch {
            delay(3000)
            findNavController().navigate(R.id.action_goto_login)
        }
    }
}
