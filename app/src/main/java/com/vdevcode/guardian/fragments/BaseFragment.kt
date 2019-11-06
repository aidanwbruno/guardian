package com.vdevcode.guardian.fragments


import android.app.Application
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.arrayMapOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.vdevcode.guardian.adapters.AppBaseAdapter
import com.vdevcode.guardian.helpers.Helper
import com.vdevcode.guardian.models.BaseModel
import com.vdevcode.guardian.view_models.AppViewModel
import kotlinx.android.synthetic.main.collapsing_toolbar.*
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.simple_toolbar.*
import org.jetbrains.annotations.NotNull
import java.lang.RuntimeException

/**
 * A simple [Fragment] subclass.
 */
abstract class BaseFragment(@NotNull protected var viewId: Int, @NotNull protected var toolbar_title: String) : Fragment() {


    protected var params = arrayMapOf<String, Any>()
    protected lateinit var appViewModel: AppViewModel
    protected var appAdapter: AppBaseAdapter? = null

    // Add all parts/methods  for this fragments
    protected abstract fun buildFragment()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setupParams()
        return inflater.inflate(viewId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(true, true)
        buildFragment()
    }


    /**
     * This method will be called on onCreate method of BaseFragment, to get All parrams sended to current fragment
     */
    protected open fun setupParams() {}

    protected open fun setupButtons() {}

    protected open fun setupToolbar(home: Boolean, homeIcon: Boolean) {
        val act = requireActivity() as AppCompatActivity
        with(act) {
            app_toolbar?.setTitleTextColor(Color.WHITE)
            setSupportActionBar(app_toolbar)
            supportActionBar?.let {
                it.title = toolbar_title
                if (home) {
                    it.setDisplayHomeAsUpEnabled(home)
                    it.setHomeButtonEnabled(homeIcon)
                }
            }
        }
    }


    protected open fun setupViewModel(model: BaseModel, params: Map<String, Any>) {
        appViewModel = ViewModelProvider(requireActivity(), AppViewModel.AppViewModelFactory(requireActivity().application, model)).get(AppViewModel::class.java)
    }

    protected open fun addObserver() {}

    protected open fun addObserver(fildLiveData: LiveData<MutableList<BaseModel>>, updateList: Boolean) {
        if (!fildLiveData.hasObservers()) {
            fildLiveData.observe(this, Observer {
                if (updateList) {
                    appAdapter?.updateList(it)
                }
            })
        }
    }

    protected open fun setupReciclerView(recyclerView: RecyclerView?, adp: AppBaseAdapter, lm: RecyclerView.LayoutManager) {
        appAdapter = adp
        recyclerView?.apply {
            layoutManager = lm
            adapter = appAdapter
        }
    }

    protected open fun setupReciclerView(recyclerView: RecyclerView?, adp: AppBaseAdapter, lm: RecyclerView.LayoutManager, newAdapter: Boolean) {
        if (!newAdapter) {
            setupReciclerView(recyclerView, adp, lm)
        } else {
            recyclerView?.apply {
                layoutManager = lm
                adapter = adp
            }
        }
    }

    /**
     * get all params from intent
     */
    protected open fun getParams(vararg keys: String) {
        arguments?.let { arg ->
            keys.forEach { key ->
                arg.get(key)?.let { ob ->
                    params.put(key, ob)
                }
            }
        }
    }


}
