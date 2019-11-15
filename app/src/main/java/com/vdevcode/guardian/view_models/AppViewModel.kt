package com.vdevcode.guardian.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vdevcode.guardian.helpers.Guardian
import com.vdevcode.guardian.models.BaseModel
import com.vdevcode.guardian.repo.AppRepo

class AppViewModel(application: Application, val model: BaseModel, var params:Map<String, Any>) :
    AndroidViewModel(application) {

    var all: LiveData<MutableList<BaseModel>>? = null

    init {
        all = AppRepo.all(model)
    }

    fun save(model: BaseModel) {
        AppRepo.insert(model)
    }

    open class AppViewModelFactory(private val mApplication: Application, private val model: BaseModel, var params:Map<String, Any>) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AppViewModel(mApplication, model, params) as T
        }
    }

}