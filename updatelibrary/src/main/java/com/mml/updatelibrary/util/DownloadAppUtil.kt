package com.mml.updatelibrary.util

import android.os.Build
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.requests.CancellableRequest
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadLargeFileListener
import com.liulishuo.filedownloader.FileDownloader
import com.mml.updatelibrary.GlobalContextProvider
import com.mml.updatelibrary.receiver.UpdateReceiver
import com.mml.updatelibrary.UpdateUtil
import java.io.File

/**
 * Author: Menglong Ma
 * Email: mml2015@126.com
 * Date: 19-10-28 下午5:57
 * Description: This is DownloadAppUtil
 * Package: com.mml.updatelibrary
 * Project: UpdateLibrary
 */
object DownloadAppUtil{
    const val TAG="DownloadAppUtil"
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
     * 下载成功回调
     */
    var onSuccess:()->Unit={}
    /**
     * 下载出错回调
     */
    var onError: () -> Unit = {}

    /**
     * 出错，点击重试回调
     */
    var onReDownload: () -> Unit = {}

    /**
     * 取消下载
     */
     var onCancel:()->Unit={}
    private const val fileName = "update.apk"
    private var http: CancellableRequest? = null
    private var downloadTask:BaseDownloadTask?=null
    fun download1() {
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
        ///sdcard/Android/data/包名
        val file =  mContext.getExternalFilesDir("update")!!//mContext.getDir("update", Context.MODE_PRIVATE)
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
                val result= (progress * 100).toInt()
                log(
                    msg = "readBytes:$readBytes  totalBytes:$totalBytes  progress:${result}",
                    tag = TAG
                )
                onProgress.invoke(result)

                UpdateReceiver.sendAction(
                    mContext,
                    UpdateReceiver.ACTION_UPDATE_PROGRESS,
                    result
                )
            }
            .response { result ->
                result.fold(
                    success = {
//                        val aa = result.component1()!!.toString(Charset.defaultCharset())
//                        log(msg = "content:$aa", tag = TAG)
                        onSuccess.invoke()
                        UpdateReceiver.sendAction(mContext, UpdateReceiver.ACTION_UPDATE_SUCCESS)
                    },
                    failure = {
                        log(msg = "content:${it.cause}", tag = TAG)
                        onError.invoke()
                        UpdateReceiver.sendAction(mContext, UpdateReceiver.ACTION_UPDATE_FAIL)
                    }
                )

            }
//        http?.join()
    }

    fun download(){
        val apkLocalPath = "${mContext.getExternalFilesDir("update")!!.path}${File.separator}$fileName"
        log("apkLocalPath:$apkLocalPath", TAG)
        if (File(apkLocalPath).exists()){
            File(apkLocalPath).delete()
        }
        FileDownloader.setup(mContext)
        downloadTask = FileDownloader.getImpl().create(
            updateInfo.apkUrl)

        downloadTask!!
            .setPath(apkLocalPath)
            .setListener(object : FileDownloadLargeFileListener() {

                override fun pending(task: BaseDownloadTask, soFarBytes: Long, totalBytes: Long) {
                    log("pending:soFarBytes($soFarBytes),totalBytes($totalBytes)",
                        TAG
                    )
                    isDownloading = true

                }

                override fun progress(task: BaseDownloadTask, soFarBytes: Long, totalBytes: Long) {
                    isDownloading = true
                    val progress = (soFarBytes * 100.0 / totalBytes).toInt()
                    log("progress:$progress", TAG)
                  onProgress.invoke(progress)
                    UpdateReceiver.sendAction(
                        mContext,
                        UpdateReceiver.ACTION_UPDATE_PROGRESS,
                        progress
                    )
                }

                override fun paused(task: BaseDownloadTask, soFarBytes: Long, totalBytes: Long) {
                    isDownloading = false
                }

                override fun completed(task: BaseDownloadTask) {
                    isDownloading = false
                    log("completed", TAG)
                    onSuccess.invoke()
                    UpdateReceiver.sendAction(mContext, UpdateReceiver.ACTION_UPDATE_SUCCESS)
                }

                override fun error(task: BaseDownloadTask, e: Throwable) {
                    isDownloading = false
                    log("error:${e.message}", TAG)
                    onError.invoke()
                    UpdateReceiver.sendAction(mContext, UpdateReceiver.ACTION_UPDATE_FAIL)
                }

                override fun warn(task: BaseDownloadTask) {
                }
            }).start()
    }
    fun cancel(){
        http?.cancel()
        downloadTask?.pause()
        onCancel.invoke()
    }
    /**
     * 出错后，点击重试
     */
    fun reTry() {
        onReDownload.invoke()
       // download()
        downloadTask?.start()
    }
}