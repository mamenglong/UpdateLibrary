package com.mml.updatelibrary.ui

import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.mml.easyconfig.showToast
import com.mml.updatelibrary.GlobalContextProvider
import com.mml.updatelibrary.data.SP
import com.mml.updatelibrary.data.UpdateInfo
import com.mml.updatelibrary.data.UpdateUrl
import com.mml.updatelibrary.log

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

    private val updateUrl=UpdateUrl()

    /**
     * @param url 链接
     *
     * 应该在 [checkUpdate]之前调用
     */
    fun setUpdateUrl(url:String): UpdateUtil {
        updateUrl.url=url
        return this
    }
    var updateInfo: UpdateInfo = UpdateInfo()
    fun checkUpdate(version:Int,onError:((msg:String)->Unit)?=null) {
        val httpAsync = updateUrl.url.httpGet()
            .responseObject<UpdateInfo> { response, _, result ->
                log(msg = "content:${response.body}", tag = "UpdateUtil")
                result.fold(success = { updateInfo ->
                    log(msg = "content:$updateInfo", tag = "UpdateUtil")
                    this.updateInfo = updateInfo
                    //设置每次显示，设置本次显示及强制更新 每次都显示弹窗
                    if (updateInfo.config.serverVersionCode > version||updateInfo.config.alwaysShow) {
                        shouldShowUpdateDialog()
                    }else{
                        showToast("暂无更新")
                    }
                }, failure = { fuelError ->
                    onError?.invoke("${fuelError.message}")
                    log(msg = "content:$fuelError", tag = "UpdateUtil")
                    if (updateInfo.config.serverVersionCode > version||updateInfo.config.alwaysShow) {
                        shouldShowUpdateDialog()
                    }else{
                        showToast("暂无更新")
                    }
                })
            }
        httpAsync.join()
    }

    private fun shouldShowUpdateDialog() {
        //检查是否忽略了
        if (updateInfo.config.alwaysShow||SP.ignoreVersion < updateInfo.config.serverVersionCode)
            UpdateActivity.start()
    }

    fun cancelNoLongerRemind() {
        SP.ignoreVersion=0
        checkUpdate(0)
    }
}