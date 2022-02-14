package com.skyd.imomoe.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewStub
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.skyd.imomoe.R
import com.skyd.imomoe.databinding.ActivityMonthAnimeBinding
import com.skyd.imomoe.util.showToast
import com.skyd.imomoe.view.adapter.variety.VarietyAdapter
import com.skyd.imomoe.view.adapter.variety.proxy.AnimeCover3Proxy
import com.skyd.imomoe.viewmodel.MonthAnimeViewModel

class MonthAnimeActivity : BaseActivity<ActivityMonthAnimeBinding>() {
    private var partUrl: String = ""
    private val viewModel: MonthAnimeViewModel by viewModels()
    private val adapter: VarietyAdapter by lazy {
        VarietyAdapter(mutableListOf(AnimeCover3Proxy()), viewModel.monthAnimeList)
    }
    private var lastRefreshTime: Long = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        partUrl = intent.getStringExtra("partUrl").orEmpty()

        mBinding.run {
            atbMonthAnimeActivity.titleText = getString(R.string.year_month_anime, partUrl)

            rvMonthAnimeActivity.layoutManager = LinearLayoutManager(this@MonthAnimeActivity)
            rvMonthAnimeActivity.setHasFixedSize(true)
            rvMonthAnimeActivity.adapter = adapter

            atbMonthAnimeActivity.setBackButtonClickListener { finish() }
            srlMonthAnimeActivity.setOnRefreshListener { //避免刷新间隔太短
                if (System.currentTimeMillis() - lastRefreshTime > 500) {
                    lastRefreshTime = System.currentTimeMillis()
                    viewModel.getMonthAnimeData(partUrl)
                } else {
                    srlMonthAnimeActivity.closeHeaderOrFooter()
                }
            }
            srlMonthAnimeActivity.setOnLoadMoreListener {
                viewModel.pageNumberBean?.let {
                    viewModel.getMonthAnimeData(it.actionUrl, isRefresh = false)
                    return@setOnLoadMoreListener
                }
                mBinding.srlMonthAnimeActivity.finishLoadMore()
                getString(R.string.no_more_info).showToast()
            }
        }

        viewModel.mldMonthAnimeList.observe(this) {
            mBinding.srlMonthAnimeActivity.closeHeaderOrFooter()
            if (it) {
                hideLoadFailedTip()
            } else {
                showLoadFailedTip(getString(R.string.load_data_failed_click_to_retry)) {
                    viewModel.getMonthAnimeData(partUrl)
                    hideLoadFailedTip()
                }
            }
            adapter.notifyDataSetChanged()
        }

        mBinding.srlMonthAnimeActivity.autoRefresh()
    }

    override fun getBinding(): ActivityMonthAnimeBinding =
        ActivityMonthAnimeBinding.inflate(layoutInflater)

    override fun getLoadFailedTipView(): ViewStub = mBinding.layoutMonthAnimeActivityLoadFailed

    @SuppressLint("NotifyDataSetChanged")
    override fun onChangeSkin() {
        super.onChangeSkin()
        adapter.notifyDataSetChanged()
    }
}
