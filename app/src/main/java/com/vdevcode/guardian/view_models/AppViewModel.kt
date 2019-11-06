package com.vdevcode.guardian.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vdevcode.guardian.helpers.Guardian
import com.vdevcode.guardian.models.BaseModel
import com.vdevcode.guardian.repo.AppRepo

class AppViewModel(application: Application, val model: BaseModel) :
    AndroidViewModel(application) {

    private var repo: AppRepo? = null
    var all: LiveData<MutableList<BaseModel>>? = null

    init {
        repo = AppRepo(Guardian.appContext!!, model)
        all = repo?.all()
    }

    fun save(model: BaseModel) {
        repo?.insert(model)
    }

    open class AppViewModelFactory(private val mApplication: Application, private val model: BaseModel) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AppViewModel(mApplication, model) as T
        }
    }

}