package com.mml.updatelibrary

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.FileProvider
import java.io.File
import kotlin.system.exitProcess


/**
 * Author: Menglong Ma
 * Email: mml2015@126.com
 * Date: 19-9-18 下午3:26
 * Description: This is Utils
 * Package: com.mml.updatelibrary
 * Project: OnceApplication
 */
fun Any.log(msg: String,tag:String="tag"){
    if (BuildConfig.DEBUG){
        Log.i(tag,msg)
    }
}
fun NotificationCompat.Builder.removeActions()=this.mActions.clear()
object Utils {
    /**
     * 安装apk
     *
     * @param context
     * @param file
     */
    @JvmStatic
    fun installApk(context: Context, file: File) {
       val authority=BuildConfig.LIBRARY_PACKAGE_NAME+".fileprovider"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        var uriData: Uri? = null
        val type = "application/vnd.android.package-archive"
        uriData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            FileProvider.getUriForFile(context, authority, file)
        } else {
            Uri.fromFile(file)
        }
        intent.setDataAndType(uriData, type)
        context.startActivity(intent)
    }

    /**
     * 安装apk
     *
     * @param context
     * @param uri
     */
    @JvmStatic
    fun installApk(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        val type = "application/vnd.android.package-archive"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        intent.setDataAndType(uri, type)
        context.startActivity(intent)
    }
    /**
     * 退出app
     */
    fun exitApp() {
        val manager = GlobalContextProvider.getGlobalContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            manager.appTasks.forEach {
                it.finishAndRemoveTask()
            }
        } else {
            exitProcess(0)
        }
    }
}
