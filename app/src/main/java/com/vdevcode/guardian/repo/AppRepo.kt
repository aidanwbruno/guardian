package com.vdevcode.guardian.repo

import android.content.Context
import androidx.lifecycle.LiveData
import com.vdevcode.guardian.database.AppDAO
import com.vdevcode.guardian.database.AppDB
import com.vdevcode.guardian.models.BaseModel
import com.vdevcode.guardian.models.Command
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AppRepo(context: Context, val model: BaseModel) {

    private var dao: AppDAO<BaseModel>
    private var db: AppDB

    init {
        db = AppDB.getDB(context)
        dao = getDAO()
    }

    fun insert(model: BaseModel) {
        dao.insert(model)
    }

    fun all(): LiveData<MutableList<BaseModel>> {
        return dao.all()
    }

    fun delete(model: BaseModel) {
        GlobalScope.launch {
            dao.delete(model)
        }
    }

    private fun getDAO(): AppDAO<BaseModel> {
        val dao = when (model) {
            is Command -> db.getWordDAO()
            else -> db.getUserDAO()
        }
        return dao as AppDAO<BaseModel>
    }

}