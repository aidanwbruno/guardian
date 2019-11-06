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
class AboutAppFragment : BaseFragment(R.layout.fragment_about_app, "Sobre o Guardian") {

    override fun buildFragment() {
    }
}
