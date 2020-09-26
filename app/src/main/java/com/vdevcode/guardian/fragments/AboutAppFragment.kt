package com.vdevcode.guardian.fragments


import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.vdevcode.guardian.R
import com.vdevcode.guardian.extensions.mhide
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_about_app.*
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
        requireActivity().ll_gps_check.mhide()
        tv_site_link.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://sosguardian.app/")))
        }
    }


}
