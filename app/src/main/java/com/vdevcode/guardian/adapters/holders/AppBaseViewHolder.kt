package com.vdevcode.guardian.adapters.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.vdevcode.guardian.models.BaseModel


open class AppBaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
     open fun onBind(model: BaseModel) {}
}