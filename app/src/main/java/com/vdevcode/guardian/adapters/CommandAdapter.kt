package com.vdevcode.guardian.adapters

import android.content.Context
import com.vdevcode.guardian.R
import com.vdevcode.guardian.adapters.holders.AppBaseViewHolder
import com.vdevcode.guardian.helpers.Guardian
import com.vdevcode.guardian.models.BaseModel
import com.vdevcode.guardian.models.Command
import com.vdevcode.guardian.repo.AppRepo
import kotlinx.android.synthetic.main.layout_command_item.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class CommandAdapter(context: Context, modelList: MutableList<BaseModel>) : AppBaseAdapter(context, modelList, R.layout.layout_command_item) {

    override fun onBindViewHolder(holder: AppBaseViewHolder, position: Int) {
        val command = modelList.get(position) as Command
        val view = holder.itemView;
        view.tv_item_command.text = command.palavra
        view.fab_command_delete.setOnClickListener {
            Guardian.dialog(context, "Atenção", "Deseja deletar o comando \"${command.palavra}\" ?", {
                GlobalScope.launch(Dispatchers.IO) {
                    async { AppRepo.delete(command) }.await()
                    GlobalScope.launch(Dispatchers.Main) {
                        Guardian.toast("Comando Deletado com Sucesso")
                    }
                }
            }, {}, "Sim", "Não").show()
        }
    }

}