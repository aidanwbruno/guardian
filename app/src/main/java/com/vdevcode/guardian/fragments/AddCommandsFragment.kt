package com.vdevcode.guardian.fragments


import androidx.fragment.app.Fragment
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.vdevcode.guardian.R
import com.vdevcode.guardian.adapters.CommandAdapter
import com.vdevcode.guardian.extensions.hideKeyword
import com.vdevcode.guardian.extensions.mhide
import com.vdevcode.guardian.helpers.Guardian
import com.vdevcode.guardian.helpers.Helper
import com.vdevcode.guardian.models.BaseModel
import com.vdevcode.guardian.models.Command
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_add_commands.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass.
 */
class AddCommandsFragment : BaseFragment(R.layout.fragment_add_commands, "Meus Comandos", true, null) {

    private var command = Command()


    override fun createFragment() {
    }

    //Called on OncreatedView
    override fun buildFragment() {
        super.setupReciclerView(rv_command_list, CommandAdapter(context!!, mutableListOf()), LinearLayoutManager(context!!))
        super.setupViewModel(Command(), params)
        super.addObserver(appViewModel.all as LiveData<MutableList<BaseModel>>, true)
        //updateAddedWordList()
        setupButtons()
        requireActivity().sw_location.mhide()
        requireActivity().tv_gps_status.mhide()
    }


    override fun setupButtons() {
        iv_save_word.setOnClickListener {
            val ok = validate()
            if (ok) {
                command.palavra = Helper.normalizeText(tiet_command_name.text.toString())
                GlobalScope.launch(Dispatchers.IO) {
                    appViewModel.save(command)
                    GlobalScope.launch(Dispatchers.Main) {
                        Guardian.toast("Comando salvo")
                        tiet_command_name.text?.clear()
                        view?.hideKeyword()
                    }
                }
            }
        }
    }


    fun validate(): Boolean {

        appAdapter?.let {
            if (it.modelList.size > 10) {
                Guardian.dialog(context!!, "Atenção", "Você só pode cadastrar no maxímo 10 comandos.", { it.dismiss() }, {}, "Ok").show()
                return false
            }
        }

        if (tiet_command_name.text.isNullOrBlank()) {
            Guardian.toast("O Coomando não pode ser vazio!")
            return false
        }

        tiet_command_name?.text?.let {
            val words = it.split(" ")
            if (it.length <= 2 || words.size > 3) {
                Guardian.toast("O tamanho do Commando deve ter no maxímo 3 palavras!")
                return false
            }
        }

        return true
    }


}
