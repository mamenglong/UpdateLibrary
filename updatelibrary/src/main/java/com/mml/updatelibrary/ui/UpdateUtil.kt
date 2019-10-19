package com.mml.updatelibrary.ui

import android.content.Intent
import android.os.Environment
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.gson.responseObject

import com.github.kittinunf.fuel.httpGet
import com.mml.easyconfig.showToast
import com.mml.updatelibrary.BuildConfig
import com.mml.updatelibrary.GlobalContextProvider
import com.mml.updatelibrary.data.SP
import com.mml.updatelibrary.data.UpdateInfo
import com.mml.updatelibrary.data.UpdateUrl
import com.mml.updatelibrary.log
import com.mml.updatelibrary.service.UpdateService
import com.mml.updatelibrary.service.UpdateService.Companion.ACTION_UPDATE_FAIL
import com.mml.updatelibrary.service.UpdateService.Companion.ACTION_UPDATE_PROGRESS
import com.mml.updatelibrary.service.UpdateService.Companion.ACTION_UPDATE_SUCCESS
import java.io.File
import java.nio.charset.Charset

/**
 * Author: Menglong Ma
 * Email: mml2015@126.com
 * Date: 19-9-17 下午3:32
 * Description: This is UpdateUtil
 * Package: com.mml.updatelibrary.data
 * Project: OnceApplication
 */
object UpdateUtil {

    init {
        GlobalContextProvider.getGlobalContext()
    }

    var updateInfo: UpdateInfo = UpdateInfo()
    fun checkUpdate(onError:((msg:String)->Unit)?=null) {
        val httpAsync = UpdateUrl().url.httpGet()
            .responseObject<UpdateInfo> { response, _, result ->
                log(msg = "content:${response.body}", tag = "UpdateUtil")
                result.fold(success = { updateInfo ->
                    log(msg = "content:$updateInfo", tag = "UpdateUtil")
                    this.updateInfo = updateInfo
                    //设置每次显示，设置本次显示及强制更新 每次都显示弹窗
                    if (updateInfo.config.serverVersionCode > BuildConfig.VERSION_CODE||updateInfo.config.alwaysShow) {
                        shouldShowUpdateDialog()
                    }else{
                        showToast("暂无更新")
                    }
                }, failure = { fuelError ->
                    onError?.invoke("${fuelError.message}")
                    log(msg = "content:$fuelError", tag = "UpdateUtil")
                })
            }
        httpAsync.join()
    }

    private fun shouldShowUpdateDialog() {
        //检查是否忽略了
        if (updateInfo.config.alwaysShow||SP.ignoreVersion < updateInfo.config.serverVersionCode)
            UpdateActivity.start()
    }

    fun sss() {
        val file = File(Environment.getDownloadCacheDirectory(), "Auto/update.apk")
        if (!file.exists())
            file.createNewFile()
        GlobalContextProvider.getGlobalContext().apply {
            UpdateService.start(this)
        }
        val http = Fuel
            .download("https://ali-fir-pro-binary.fir.im/b725376798430078f69d0558131662c09b1f6a38.apk?auth_key=1569383235-0-0-e7c886fe18f51a517958451bdbb04f2c")
            .fileDestination { response, url ->
                file
            }
            .progress { readBytes, totalBytes ->
                val process = readBytes.toFloat() / totalBytes.toFloat()
                GlobalContextProvider.getGlobalContext()
                    .sendBroadcast(Intent(ACTION_UPDATE_PROGRESS).apply {
                        log(
                            msg = "readBytes:$readBytes  totalBytes:$totalBytes  process:${(process * 100).toInt()}",
                            tag = "UpdateUtil"
                        )
                        putExtra("process", (process * 100).toInt())
                    })
            }
            .response { result ->
                result.fold(
                    success = {
                        val aa = result.component1()!!.toString(Charset.defaultCharset())
                        log(msg = "content:$aa", tag = "UpdateUtil")
                        GlobalContextProvider.getGlobalContext()
                            .sendBroadcast(Intent(ACTION_UPDATE_SUCCESS))
                    },
                    failure = {
                        GlobalContextProvider.getGlobalContext()
                            .sendBroadcast(Intent(ACTION_UPDATE_FAIL))
                    }
                )

            }

    }

    fun cancelNoLongerRemind() {
        SP.ignoreVersion=0
        checkUpdate()
    }
}