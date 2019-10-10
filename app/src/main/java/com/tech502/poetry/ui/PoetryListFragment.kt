package com.tech502.poetry.ui

import android.os.Bundle
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.tech502.poetry.model.Poetry
import com.tech502.poetry.model.isSuccess
import com.tech502.poetry.ui.adapter.PoetryAdapter
import com.tech502.poetry.util.HttpUtil
import com.tech502.poetry.util.Utils
import com.zqc.opencc.android.lib.ChineseConverter
import com.zqc.opencc.android.lib.ConversionType
import kotlinx.coroutines.*

/**
 * Created by guoziwei on 2018/4/26 0026.
 */
class PoetryListFragment : ListFragment<Poetry>(), CoroutineScope by MainScope() {

    private var queryKey: String? = ""

    companion object {
        fun newInstance(queryKey: String): PoetryListFragment {
            val fragment = PoetryListFragment()
            val args = Bundle()
            args.putString("data", queryKey)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        queryKey = arguments?.getString("data")
    }

    override fun getAdapter(): BaseQuickAdapter<Poetry, out BaseViewHolder> {
        val adapter = PoetryAdapter()
        adapter.setOnItemClickListener { a, view, position ->
            ContentActivity.launch(view.context, mAdapter.data[position])
        }
        adapter.setEnableLoadMore(true)
        return adapter
    }

    override fun loadData() {
        launch {
            try {
                val it = withContext(Dispatchers.IO) {
                    HttpUtil.create().searchPoetry(ChineseConverter.convert(queryKey, ConversionType.S2T, context), mPage)
                }
                if (it.isSuccess()) {
                    loadDataSuccess(it.data)
                } else {
                    Utils.showToast(mContext, it.msg)
                    loadFailed()
                }
            } catch (e: Exception) {
                Utils.showToast(mContext, e.message)
                loadFailed()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}