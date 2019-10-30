package com.mml.updatelibrary

import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.mml.easyconfig.showToast
import com.mml.updatelibrary.data.SP
import com.mml.updatelibrary.data.UpdateInfo
import com.mml.updatelibrary.data.UpdateUrl
import com.mml.updatelibrary.ui.UpdateActivity
import com.mml.updatelibrary.util.DownloadAppUtil.onError
import com.mml.updatelibrary.util.log

/**
 * Author: Menglong Ma
 * Email: mml2015@126.com
 * Date: 19-9-17 下午3:32
 * Description: This is UpdateUtil
 * Package: com.mml.updatelibrary.data
 * Project: OnceApplication
 */
object UpdateUtil {
    private var isLocal = false
    private var nowVersion = 0
    /**
     * 控制是否显示日志
     */
    var isDebug = false

    init {
        GlobalContextProvider.getGlobalContext()
    }

    /**
     * 网络请求获取配置时使用
     */
    var onError: ((msg: String) -> Unit)? = null
    /**
     * 没有更新回调
     */
    var onNoUpdate:()->Unit={  showToast("暂无更新") }
    private val updateUrl = UpdateUrl()
    /**
     * 全局使用的配置信息
     */
    var updateInfo: UpdateInfo = UpdateInfo()

    /**
     * 设置远程配置存放路径,设置以后使用网络请求获取远程配置
     * @param url 链接
     *
     * 应该在 [checkUpdate]之前调用
     */
    fun setUpdateUrl(url: String): UpdateUtil {
        updateUrl.url = url
        isLocal=false
        return this
    }

    /**
     *  设置本地更新信息,设置以后不在执行网络请求获取配置
     * @param updateInfo
     */
    fun setUpdateConfigInfo(updateInfo: UpdateInfo): UpdateUtil  {
        this.updateInfo = updateInfo
        isLocal = true
        return this
    }

    /**
     * 最终调用这个进行检查更新
     */
    fun checkUpdate() {
        if (BuildConfig.DEBUG) {
            shouldShowUpdateDialog()
            return
        }
        if (isLocal) {
            updateInfoFromLocal()
        } else {
            updateInfoFromWeb()
        }
    }

    private fun updateInfoFromLocal() {
        shouldShowUpdateDialog()
    }

    private fun updateInfoFromWeb() {
        val httpAsync = updateUrl.url.httpGet()
            .responseObject<UpdateInfo> { response, _, result ->
                log(msg = "content:${response.body}", tag = "UpdateUtil")
                result.fold(success = { updateInfo ->
                    log(msg = "content:$updateInfo", tag = "UpdateUtil")
                    UpdateUtil.updateInfo = updateInfo
                    shouldShowUpdateDialog()
                }, failure = { fuelError ->
                    onError?.invoke("${fuelError.message}")
                    log(msg = "content:$fuelError", tag = "UpdateUtil")
                })
            }
        httpAsync.join()
    }

    /**
     * 显示更新弹窗
     */
    private fun shouldShowUpdateDialog() {
        //设置每次显示，设置本次显示及强制更新 每次都显示弹窗
        if (updateInfo.config.serverVersionCode > nowVersion || updateInfo.config.alwaysShow) {
            if (SP.ignoreVersion < updateInfo.config.serverVersionCode) {
                UpdateActivity.start()
            }
        } else {
            onNoUpdate.invoke()
        }
    }

    fun cancelNoLongerRemind() {
        SP.ignoreVersion = 0
        checkUpdate()
    }

    @JvmStatic
    fun getInstence()=this
}