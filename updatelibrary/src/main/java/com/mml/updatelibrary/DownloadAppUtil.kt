package com.mml.updatelibrary

import android.content.Context
import android.os.Build
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.requests.CancellableRequest
import com.mml.updatelibrary.receiver.UpdateReceiver
import com.mml.updatelibrary.ui.UpdateUtil
import java.io.File
import java.nio.charset.Charset

/**
 * Author: Menglong Ma
 * Email: mml2015@126.com
 * Date: 19-10-28 下午5:57
 * Description: This is DownloadAppUtil
 * Package: com.mml.updatelibrary
 * Project: UpdateLibrary
 */
object DownloadAppUtil{
    /**
     * 更新信息
     */
    private val updateInfo by lazy { UpdateUtil.updateInfo }

    /**
     * context
     */
    private val mContext by lazy { GlobalContextProvider.getGlobalContext() }

    /**
     * 是否在下载中
     */
    var isDownloading = false

    /**
     *下载进度回调
     */
    var onProgress: (Int) -> Unit = {}

    /**
     * 下载出错回调
     */
    var onError: () -> Unit = {}

    /**
     * 出错，点击重试回调
     */
    var onReDownload: () -> Unit = {}

    private val fileName = "update.apk"
    private var http: CancellableRequest? = null
    fun download() {
        ///data/data/包名/files
        // /data/user/0/包名/files
        mContext.filesDir
        // /data/user/0/包名/cache
        mContext.cacheDir
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // /data/user/0/包名
            mContext.dataDir
        }
        // /storage/emulated/0/Android/obb/包名
        mContext.obbDir
        // /storage/emulated/0/Android/data/包名/cache
        mContext.externalCacheDir
        // /data/user/0/com.mml.demo/app_update
        val file = mContext.getDir("update", Context.MODE_PRIVATE)
        if (!file.exists()) {
            file.mkdirs()
        }
        val update = File(file, fileName)
        if (!update.exists())
            update.createNewFile()
        http = Fuel
            .download(updateInfo.apkUrl)

            .fileDestination { response, url ->
                update
            }
            .progress { readBytes, totalBytes ->
                val progress = readBytes.toFloat() / totalBytes.toFloat()
                log(
                    msg = "readBytes:$readBytes  totalBytes:$totalBytes  progress:${(progress * 100).toInt()}",
                    tag = "UpdateActivity"
                )
                UpdateReceiver.sendAction(
                    mContext,
                    UpdateReceiver.ACTION_UPDATE_PROGRESS,
                    (progress * 100).toInt()
                )
            }
            .response { result ->
                result.fold(
                    success = {
                        val aa = result.component1()!!.toString(Charset.defaultCharset())
                        log(msg = "content:$aa", tag = "UpdateActivity")
                        UpdateReceiver.sendAction(mContext, UpdateReceiver.ACTION_UPDATE_SUCCESS)
                    },
                    failure = {
                        log(msg = "content:${it.cause}", tag = "UpdateActivity")
                        UpdateReceiver.sendAction(mContext, UpdateReceiver.ACTION_UPDATE_FAIL)
                    }
                )

            }
        http?.join()
    }

    fun cancel(){
        http?.cancel()
    }
    /**
     * 出错后，点击重试
     */
    fun reTry() {
        onReDownload.invoke()
        download()
    }
}