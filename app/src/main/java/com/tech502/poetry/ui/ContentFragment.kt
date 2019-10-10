package com.tech502.poetry.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.tech502.poetry.R
import com.tech502.poetry.model.Poetry
import com.tech502.poetry.model.PoetryContentViewModel
import com.tech502.poetry.util.Utils
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.runtime.Permission
import kotlinx.android.synthetic.main.fragment_content.view.*
import kotlinx.coroutines.*


/**
 * Created by guoziwei on 2018/4/24 0024.
 */
class ContentFragment : BaseFragment(), CoroutineScope by MainScope() {


    private lateinit var poetry: Poetry

    companion object {
        fun newInstance(poetry: Poetry): ContentFragment {
            val fragment = ContentFragment()
            val args = Bundle()
            args.putParcelable("data", poetry)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        poetry = arguments?.getParcelable("data") as Poetry
    }

    private val viewModel: PoetryContentViewModel by lazy {
        ViewModelProviders.of(this).get(PoetryContentViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v: View = inflater.inflate(R.layout.fragment_content, container, false)

        v.scrollView.post { v.scrollView.fullScroll(View.FOCUS_RIGHT) }

        Utils.setText(v.tv_content, poetry.content)
        val dynasty = when (poetry.dynasty) {
            "T" -> "唐"
            "S" -> "宋"
            else -> ""
        }
        Utils.setText(v.tv_author, "︻$dynasty︼  ${poetry.author}")
        Utils.setText(v.tv_title, poetry.title)

        v.tv_author_intro.setOnClickListener { PoemActivity.launch(v.context, poetry.author_id, poetry.author) }
        v.tv_share.setOnClickListener {
            AndPermission.with(this@ContentFragment)
                    .runtime()
                    .permission(Permission.Group.STORAGE)
                    .onGranted {
                        share(v.scrollView)
                    }
                    .onDenied {
                        Utils.showToast(mContext, "分享失败，请打开读写手机存储的权限")
                    }
                    .start()

        }
        v.tv_collect.setOnClickListener { viewModel.setCollect(mContext, poetry) }

        viewModel.isCollect.observe(this, Observer {
            v.tv_collect.setText(if (it) R.string.cancel_collect else R.string.collect)
        })
        viewModel.message.observe(this, Observer { Utils.showToast(mContext, it) })
        return v
    }


    override fun onResume() {
        super.onResume()
        viewModel.loadCollect(mContext, poetry)
    }


    private fun share(v: ViewGroup) = launch(Utils.defaultCoroutineExceptionHandler(mContext)) {
        val it = withContext(Dispatchers.IO) {
            Utils.saveScreenshot(v.getChildAt(0), v.getChildAt(0)!!.width,
                    v.getChildAt(0)!!.height)
        }
        val sharingIntent = Intent(Intent.ACTION_SEND)
        val screenshotUri = FileProvider.getUriForFile(
                mContext,
                mContext.packageName + ".provider",
                it)

        sharingIntent.type = "image/jpeg"
        sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri)
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_text)))
    }


    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}