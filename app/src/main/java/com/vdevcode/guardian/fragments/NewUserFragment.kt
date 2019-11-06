package com.vdevcode.guardian.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.vdevcode.guardian.R
import com.vdevcode.guardian.auth.AppAuth
import com.vdevcode.guardian.database.AppFireDB
import com.vdevcode.guardian.extensions.mhide
import com.vdevcode.guardian.extensions.mshow
import com.vdevcode.guardian.helpers.Guardian
import com.vdevcode.guardian.models.AppUser
import kotlinx.android.synthetic.main.fragment_new_user.*

/**
 * A simple [Fragment] subclass.
 */
class NewUserFragment : BaseFragment(R.layout.fragment_new_user, "Cadastro de Usuário") {

    private val user = AppUser()

    override fun buildFragment() {
        setupButtons()
    }

    override fun setupButtons() {
        fab_save_user.setOnClickListener {
            if (validate()) {
                AppAuth.createUserAccount(buildUserTest(), {
                    Guardian.toast("Conta criada com sucesso")
                    findNavController().navigate(R.id.action_user_created)
                })
            }
        }
        cb_has_car.setOnCheckedChangeListener { compoundButton, ok ->
            user.carro = ok
            if (ok) ll_has_car.mshow() else ll_has_car.mhide()
        }

        cb_diver.setOnCheckedChangeListener { compoundButton, ok ->
            user.motorista = ok
        }
    }

    private fun buildUser() = AppUser().apply {
        user.apply {
            email = et_newuser_email.text.toString()
            email = et_newuser_date.text.toString()
            password = et_newuser_pass.text.toString()
            cep = et_newuser_cep.text.toString()
            cpf = et_newuser_cpf.text.toString()
            rua = et_newuser_rua.text.toString()
            numero = et_newuser_num.text.toString()
            bairro = et_newuser_bairro.text.toString()
            cidade = et_newuser_city.text.toString()
            estado = et_newuser_uf.text.toString()
            if (et_newuser_marca.text.toString().isNotBlank()) {
                marca = et_newuser_marca.text.toString()
            }
            if (et_newuser_model.text.toString().isNotBlank()) {
                modelo = et_newuser_model.text.toString()
            }
            if (et_newuser_cor.text.toString().isNotBlank()) {
                cor = et_newuser_cor.text.toString()
            }
        }
    }


    private fun buildUserTest() = user.apply {
        name = "Miguel Vieira"
        email = "vn1miguel@test.com"
        password = "111111"
        cep = "111111"
        cpf = "1234567905"
        rua = "rua do miguel"
        numero = "123"
        bairro = "bairro do miguel"
        cidade = "cidade do miguel"
        estado = "estado do miguel"
        if (et_newuser_marca.text.toString().isNotBlank()) {
            marca = "Caroo 9991"
        }
        if (et_newuser_model.text.toString().isNotBlank()) {
            modelo = "Modelo 2019"
        }
        if (et_newuser_cor.text.toString().isNotBlank()) {
            cor = "Preta"
        }
    }


    private fun validate(): Boolean {
        val fieldsOk = til_user_name.ok(et_newuser_name, "Nome") &&
                til_user_email.ok(et_newuser_email, "Email") &&
                til_user_date.ok(et_newuser_date, "Nascimento") &&
                til_user_pass.ok(et_newuser_pass, "Senha") &&
                til_user_cep.ok(et_newuser_cep, "CEP") &&
                til_user_cpf.ok(et_newuser_cpf, "CPF") &&
                til_user_rua.ok(et_newuser_rua, "Rua") &&
                til_user_num.ok(et_newuser_num, "Número") &&
                til_user_bairro.ok(et_newuser_bairro, "Bairro") &&
                til_user_city.ok(et_newuser_city, "Cidade") &&
                til_user_uf.ok(et_newuser_uf, "Estado")
        val pass = et_newuser_pass.text.toString()
        val checkPass = et_newuser_checkpass.text.toString()

        if (!pass.equals(checkPass)) {
            Guardian.snack(view!!, "As senhas informadas não são iguais")
            return false
        }

        return true //fieldsOk

    }

    private fun TextInputLayout.ok(et: TextInputEditText, field: String): Boolean {
        if (et.text.toString().isBlank()) {
            this.error = "O campo $field  é obrigatório"
            Guardian.snack(view!!, "O campo $field  é obrigatório")
            return false
        }
        return true
    }
}
