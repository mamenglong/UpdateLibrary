package com.mml.updatelibrary.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.requests.CancellableRequest
import com.liulishuo.filedownloader.model.FileDownloadStatus.progress
import com.mml.easyconfig.extSetVisibility
import com.mml.updatelibrary.*
import com.mml.updatelibrary.GlobalContextProvider
import com.mml.updatelibrary.data.SP
import com.mml.updatelibrary.data.UpdateInfo
import com.mml.updatelibrary.receiver.UpdateReceiver
import com.mml.updatelibrary.service.UpdateService
import kotlinx.android.synthetic.main.activity_update.*
import java.io.File
import java.nio.charset.Charset

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
                }
            }
            onProgress={
                runOnUiThread {
                    btn_group.visibility = View.GONE
                    progress_group.visibility = View.VISIBLE
                    btn_update_result.visibility=View.GONE
                    progress_bar.progress = it
                    tv_progress_text.text = getString(R.string.tv_progress_text, "$it%")
                }
            }
            onSuccess={
                runOnUiThread {
                    btn_group.visibility = View.GONE
                    progress_group.visibility = View.GONE
                    btn_update_result.visibility=View.VISIBLE
                    btn_update_result.text="下载成功,点击安装"
                    btn_update_result.setOnClickListener {
                        log("installApk", TAG)
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
                }
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

    private fun initView() {
        if (updateInfo.updateTitle.isNotEmpty())
            tv_update_title.text = updateInfo.updateTitle
        if (updateInfo.updateContent.isNotEmpty())
            tv_update_content.text = updateInfo.updateContent
        btn_update_sure.setOnClickListener {
            UpdateService.start(this)
            DownloadAppUtil.download()

        }
        btn_update_cancel.setOnClickListener {
            SP.ignoreVersion = updateInfo.config.serverVersionCode
            finish()
        }
    }

    override fun onBackPressed() {
        if (updateInfo.config.force) {
            Utils.exitApp()
        } else {
            super.onBackPressed()
        }
    }


}
