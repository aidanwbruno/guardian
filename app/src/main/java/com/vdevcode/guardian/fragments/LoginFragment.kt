package com.vdevcode.guardian.fragments


import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vdevcode.guardian.R
import com.vdevcode.guardian.auth.AppAuth
import com.vdevcode.guardian.helpers.Guardian
import kotlinx.android.synthetic.main.fragment_login.*

/**
 * A simple [Fragment] subclass.
 */
class LoginFragment : BaseFragment(R.layout.fragment_login, "Guardian Login", false, null) {

    override fun homeIconClicked() {
    }

    override fun buildFragment() {
        setupButtons()
    }

    override fun onResume() {
        super.onResume()
        if (AppAuth.isUserLogged()) {
            findNavController().navigate(R.id.action_login_ok)
        }
    }

    // Cards Views Clicables
    override fun setupButtons() {
        mb_login.setOnClickListener {
            if (validateLogin()) {
                AppAuth.singInWithEmailAndPassword(et_login_email.text.toString(), et_login_password.text.toString()) {
                    Guardian.toast("Bem Vindo ao Guardian")
                    findNavController().navigate(R.id.action_login_ok)
                }
            }
        }
        mb_goto_create_user.setOnClickListener { findNavController().navigate(R.id.action_goto_new_user) }
        mb_recover_pass.setOnClickListener {
            Guardian.dialog(context!!, "Recuperar Senha", "Entre em contato com administrador do Aplicativo para recuperar a sua senha", { it.dismiss() }, {}, "ok").show()
        }
    }


    private fun validateLogin(): Boolean {
        if (et_login_email.text.isNullOrBlank()) {
            Guardian.toast("Informe uma email valido")
            return false
        }

        if (et_login_password.text.isNullOrBlank()) {
            Guardian.toast("informe a sua senha")
            return false
        }

        if (et_login_password.text.toString().length < 6) {
            Guardian.toast("Sua senha deve ter pelo menos 6 digitos")
            return false
        }

        return true
    }


}
