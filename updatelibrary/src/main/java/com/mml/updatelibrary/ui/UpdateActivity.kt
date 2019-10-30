package com.mml.updatelibrary.ui

import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import androidx.appcompat.app.AppCompatActivity
import com.mml.updatelibrary.*
import com.mml.updatelibrary.GlobalContextProvider
import com.mml.updatelibrary.data.SP
import com.mml.updatelibrary.data.UpdateInfo
import com.mml.updatelibrary.leaf.AnimationUtils
import com.mml.updatelibrary.receiver.UpdateReceiver
import com.mml.updatelibrary.service.UpdateService
import com.mml.updatelibrary.util.DownloadAppUtil
import com.mml.updatelibrary.util.Utils
import com.mml.updatelibrary.util.log
import kotlinx.android.synthetic.main.activity_update.*
import java.io.File

class UpdateActivity : AppCompatActivity() {

    private lateinit var updateInfo: UpdateInfo
    companion object {
        fun start() {
            log(msg = "content:start", tag = "UpdateActivity")
            val context = GlobalContextProvider.getGlobalContext().applicationContext
            val intent = Intent(context, UpdateActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)
        initUi()
        updateInfo = UpdateUtil.updateInfo
        log(msg = "content:$updateInfo", tag = "UpdateActivity")
        initView()
        initConfig()
        initDownload()
    }

    override fun onDestroy() {
        super.onDestroy()
        DownloadAppUtil.cancel()
        UpdateReceiver.sendAction(this, UpdateReceiver.ACTION_UPDATE_CANCEL)
    }
    private fun initView() {
        if (updateInfo.updateTitle.isNotEmpty())
            tv_update_title.text = updateInfo.updateTitle
        if (updateInfo.updateContent.isNotEmpty())
            tv_update_content.text = updateInfo.updateContent
        btn_update_sure.setOnClickListener {
            if (updateInfo.config.isShowNotification)
                  UpdateService.start(this)
            DownloadAppUtil.download()
            fan_pic.startAnimation(AnimationUtils.initRotateAnimation(
                false, 1500, true,
                Animation.INFINITE)
            )
        }
        btn_update_cancel.setOnClickListener {
            SP.ignoreVersion = updateInfo.config.serverVersionCode
            finish()
        }
    }
    private fun initConfig() {
        if (updateInfo.config.force) {
            btn_update_cancel.visibility = View.GONE
        } else {
            if (SP.ignoreVersion < updateInfo.config.serverVersionCode) {
                btn_update_cancel.visibility = View.VISIBLE
            } else {
                btn_update_cancel.visibility = View.GONE
                finish()
            }
        }
    }

    private fun initDownload(){
        DownloadAppUtil.apply {
            onError={
                runOnUiThread {
                    btn_group.visibility = View.GONE
                    progress_group.visibility = View.GONE
                    btn_update_result.visibility=View.VISIBLE
                    btn_update_result.text="下载失败,点击重试"
                    btn_update_result.setOnClickListener {
                        DownloadAppUtil.reTry()
                    }
                    leaf_progress.visibility=View.GONE
                }
            }
            onProgress={
                runOnUiThread {

                    btn_group.visibility = View.GONE
                    progress_group.visibility = View.VISIBLE
                    btn_update_result.visibility=View.GONE
                    progress_bar.progress = it
                    tv_progress_text.text = getString(R.string.tv_progress_text, "$it%")
                    leaf_loading.setProgress(it)
                }
            }
            onSuccess={
                runOnUiThread {
                    btn_group.visibility = View.GONE
                    progress_group.visibility = View.GONE
                    btn_update_result.visibility=View.VISIBLE
                    btn_update_result.text="下载成功,点击安装"
                    leaf_progress.visibility=View.GONE
                    btn_update_result.setOnClickListener {
                        log("installApk", TAG)
                        UpdateReceiver.sendAction(this@UpdateActivity,UpdateReceiver.ACTION_UPDATE_CANCEL)
                        Utils.installApk(this@UpdateActivity,File(getExternalFilesDir("update")!!,"update.apk"))
                    }
                }
            }
            onReDownload={
                runOnUiThread{
                    btn_group.visibility = View.GONE
                    btn_update_result.visibility=View.GONE
                    progress_group.visibility = View.VISIBLE
                    progress_bar.progress = 0
                    tv_progress_text.text = getString(R.string.tv_progress_text, "0%")
                    leaf_loading.setProgress(0)
                }
            }
            onCancel={
                btn_group.visibility = View.VISIBLE
                progress_group.visibility = View.GONE
                btn_update_result.visibility=View.GONE
            }
        }
    }

    private fun initUi() {
        val d = windowManager.defaultDisplay // 为获取屏幕宽、高
        val p = window.attributes
        val point = Point()
        d.getSize(point)
        p.height = ((point.y * 0.4).toInt()) // 高度设置为屏幕的0.3
        p.width = ((point.x * 0.7).toInt()) // 宽度设置为屏幕的0.7
        window.attributes = p
    }



    override fun onBackPressed() {
        if (updateInfo.config.force) {
            Utils.exitApp()
        } else {
            super.onBackPressed()
        }
    }


}
