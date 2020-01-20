package com.vdevcode.guardian.fragments


import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.vdevcode.guardian.R
import kotlinx.android.synthetic.main.simple_toolbar.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 */
class AboutAppFragment : BaseFragment(R.layout.fragment_about_app, "Sobre o Guardian", true, null) {

    override fun homeIconClicked() {
        findNavController().popBackStack()
    }

    override fun buildFragment() {
    }

}
