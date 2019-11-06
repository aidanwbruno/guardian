package com.vdevcode.guardian.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.vdevcode.guardian.adapters.holders.AppBaseViewHolder
import com.vdevcode.guardian.models.BaseModel


abstract class AppBaseAdapter(val context: Context, var modelList: MutableList<BaseModel>, private val layoutId: Int) : RecyclerView.Adapter<AppBaseViewHolder>() {

    // make diifutils
    fun updateList(newList: MutableList<BaseModel>) {
        modelList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppBaseViewHolder {
        return AppBaseViewHolder(LayoutInflater.from(context).inflate(layoutId, parent, false))
    }

    override fun getItemCount(): Int {
        return modelList.size
    }

}