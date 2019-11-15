package com.vdevcode.guardian.fragments


import android.app.Application
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.arrayMapOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
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
abstract class BaseFragment(@NotNull protected var viewId: Int, @NotNull protected var title: String, protected var back: Boolean, protected var icon: Int?) : Fragment() {


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
        setupToolbar()
        buildFragment()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> findNavController().popBackStack()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * This method will be called on onCreate method of BaseFragment, to get All parrams sended to current fragment
     */
    protected open fun setupParams() {}

    protected open fun setupButtons() {}

    protected open fun setupToolbar(vararg iconClick: (View?) -> Unit) {
        (requireActivity() as AppCompatActivity).let {
            app_toolbar?.title = title.toUpperCase()

            icon?.let {
                app_toolbar.setNavigationIcon(it)
            }
            // app_toolbar?.setNavigationOnClickListener(View.OnClickListener { Vocc.toast("sdsds") })
            //app_toolbar?.setupWithNavController(findNavController(), AppBarConfiguration(findNavController().graph))
            it.setSupportActionBar(app_toolbar)
            it.supportActionBar?.run {
                if (back) {
                    setDisplayHomeAsUpEnabled(true)
                    setHomeButtonEnabled(true)
                    setDisplayShowHomeEnabled(true)
                }

            }
            setHasOptionsMenu(true)
        }

    }


    protected open fun setupViewModel(model: BaseModel, params: Map<String, Any>) {
        appViewModel = ViewModelProvider(this, AppViewModel.AppViewModelFactory(requireActivity().application, model, params)).get(AppViewModel::class.java)
    }

    protected open fun addObserver() {}

    protected open fun addObserver(fildLiveData: LiveData<MutableList<BaseModel>>, updateList: Boolean) {
        if (!fildLiveData.hasObservers()) {
            fildLiveData.observe(requireActivity(), Observer {
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
