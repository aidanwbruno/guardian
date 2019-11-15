package com.vdevcode.guardian.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.vdevcode.guardian.R
import com.vdevcode.guardian.auth.AppAuth
import com.vdevcode.guardian.database.AppFireDB
import com.vdevcode.guardian.extensions.mhide
import com.vdevcode.guardian.extensions.mshow
import com.vdevcode.guardian.helpers.Guardian
import com.vdevcode.guardian.models.AppUser
import com.vdevcode.guardian.models.Car
import com.vdevcode.guardian.extensions.*
import kotlinx.android.synthetic.main.fragment_new_user.*

/**
 * A simple [Fragment] subclass.
 */
class NewUserFragment : BaseFragment(R.layout.fragment_new_user, "Cadastro de Usuário", true, null) {

    private val user = AppUser()
    private var carList = ""

    override fun setupParams() {
        requireActivity().onBackPressedDispatcher.addCallback {
            Guardian.dialog(context!!, "Atenção", "Deseja sair da tela de cadastro?", {
                findNavController().popBackStack()
            }, {}, "Sim", "Não").show()
        }
    }

    override fun buildFragment() {
        setupButtons()
    }

    override fun setupButtons() {
        fab_save_user.setOnClickListener {
            if (validate()) {
                if (ll_has_car.visible() && user.CARROS.isNullOrEmpty()) {
                    Guardian.dialog(context!!, "Atenção", "Você, abriu os campos de cadastro de veículo, deseja adicionar algum, antes de finalizar?", {
                        it.dismiss()
                    }, {
                        Guardian.snack(view!!, "Aguarde.. Estamos criando a sua conta :)")
                        AppAuth.createUserAccount(buildUser()) {
                            Guardian.toast("Conta criada com sucesso!")
                            findNavController().navigate(R.id.action_user_created)
                        }
                    }, "Adicionar", "Continuar").show()
                } else {
                    Guardian.snack(view!!, "Aguarde.. Estamos criando a sua conta :)")
                    AppAuth.createUserAccount(buildUser()) {
                        Guardian.toast("Conta criada com sucesso!")
                        findNavController().navigate(R.id.action_user_created)
                    }
                }
            }
        }
        cb_has_car.setOnCheckedChangeListener { compoundButton, ok ->
            if (ok) ll_has_car.mshow() else ll_has_car.mhide()
        }

        cb_diver.setOnCheckedChangeListener { compoundButton, ok ->
            user.motorista = ok
        }

        mb_add_new_car.setOnClickListener {
            if (validateCar()) {
                addCar()
            }
        }
    }

    private fun buildUser() = user.apply {
        name = et_newuser_name.text.toString()
        email = et_newuser_email.text.toString()
        nascimento = et_newuser_date.text.toString()
        password = et_newuser_pass.text.toString()
        cep = et_newuser_cep.text.toString()
        cpf = et_newuser_cpf.text.toString()
        rua = et_newuser_rua.text.toString()
        numero = et_newuser_num.text.toString()
        bairro = et_newuser_bairro.text.toString()
        cidade = et_newuser_city.text.toString()
        estado = et_newuser_uf.text.toString()
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
        CARROS?.put("carrro1111", Car("CHEVROLET ", "Camaro", "preta", "xxx-xxXX"))
        if (et_newuser_marca.text.toString().isNotBlank()) {
            CARROS?.put("carrro1111", Car("CHEVROLET ", "Camaro", "preta", "xxx-xxXX"))
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
                til_user_uf.ok(et_newuser_uf, "Estado") &&
                til_user_complemento.ok(et_newuser_uf, "Complemento")
        val pass = et_newuser_pass.text.toString()
        val checkPass = et_newuser_checkpass.text.toString()

        if (!pass.equals(checkPass)) {
            Guardian.snack(view!!, "As senhas informadas não são iguais")
            return false
        }

        return fieldsOk

    }

    private fun TextInputLayout.ok(et: TextInputEditText, field: String): Boolean {
        if (et.text.toString().isBlank()) {
            this.error = "O campo $field  é obrigatório"
            Guardian.snack(view!!, "O campo $field  é obrigatório")
            return false
        }
        return true
    }


    private fun validateCar() = til_user_placa.ok(et_newuser_placa, "Placa") &&
            til_user_marca.ok(et_newuser_marca, "Marca") &&
            til_user_model.ok(et_newuser_model, "Modelo") &&
            til_user_cor.ok(et_newuser_cor, "Cor")


    private fun addCar() {
        val car = Car()
        if (et_newuser_marca.text.toString().isNotBlank()) {
            car.marca = et_newuser_marca.text.toString()
        }
        if (et_newuser_model.text.toString().isNotBlank()) {
            car.modelo = et_newuser_model.text.toString()
        }
        if (et_newuser_cor.text.toString().isNotBlank()) {
            car.cor = et_newuser_cor.text.toString()
        }
        if (et_newuser_cor.text.toString().isNotBlank()) {
            car.placa = et_newuser_cor.text.toString()
        }
        user.CARROS?.put(car.marca.plus(car.modelo), car)
        carList += "Carrro: ${car.marca}, ${car.modelo}, ${car.cor}, placa: ${car.placa} \n"
        tv_mycars_list.text = carList
        Guardian.snack(view!!, "Carro adicionado")
        et_newuser_placa.text?.clear()
        et_newuser_marca.text?.clear()
        et_newuser_model.text?.clear()
        et_newuser_cor.text?.clear()
    }

}
