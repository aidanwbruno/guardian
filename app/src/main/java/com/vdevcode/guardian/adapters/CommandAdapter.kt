package com.vdevcode.guardian.adapters

import android.content.Context
import com.vdevcode.guardian.R
import com.vdevcode.guardian.adapters.holders.AppBaseViewHolder
import com.vdevcode.guardian.models.BaseModel
import com.vdevcode.guardian.models.Command


class CommandAdapter(context: Context, modelList: MutableList<BaseModel>) : AppBaseAdapter(context, modelList, R.layout.activity_main) {

    override fun onBindViewHolder(holder: AppBaseViewHolder, position: Int) {
        val word = modelList.get(position) as Command
        val view = holder.itemView;
    }

}