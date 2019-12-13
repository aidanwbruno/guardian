package com.vdevcode.guardian.repo

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.LiveData
import com.vdevcode.guardian.database.AppDAO
import com.vdevcode.guardian.database.AppDB
import com.vdevcode.guardian.helpers.Guardian
import com.vdevcode.guardian.models.BaseModel
import com.vdevcode.guardian.models.Command
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object AppRepo {

    private var db: AppDB

    init {
        db = AppDB.getDB(Guardian.appContext!!)
    }

    fun insert(model: BaseModel) {
        try {
            val dao = getDAO(model)
            dao.insert(model)
        } catch (ex: SQLiteConstraintException) {
            GlobalScope.launch(Dispatchers.Main) {
                Guardian.toast("Item já está cadastrado")
            }
        }
    }

    fun update(model: BaseModel) {
        val dao = getDAO(model)
        dao.update(model)
    }

    fun all(model: BaseModel): LiveData<MutableList<BaseModel>> {
        val dao = getDAO(model)
        return dao.all()
    }

    fun getAllCommands(): MutableList<Command> {
        return db.getCommandDAO().findAllCommands()
    }

    fun delete(model: BaseModel) {
        val dao = getDAO(model)
        GlobalScope.launch {
            dao.delete(model)
        }
    }

    private fun getDAO(model: BaseModel): AppDAO<BaseModel> {
        val dao = when (model) {
            is Command -> db.getCommandDAO()
            else -> db.getUserDAO()
        }
        return dao as AppDAO<BaseModel>
    }

}