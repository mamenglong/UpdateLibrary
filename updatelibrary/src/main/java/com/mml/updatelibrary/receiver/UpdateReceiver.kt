package com.mml.updatelibrary.receiver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.liulishuo.filedownloader.util.DownloadServiceNotConnectedHelper.startForeground
import com.mml.updatelibrary.*
import com.mml.updatelibrary.GlobalContextProvider
import java.io.File

/**
 * Author: Menglong Ma
 * Email: mml2015@126.com
 * Date: 19-10-28 下午4:38
 * Description: This is UpdateReceiver
 * Package: com.mml.updatelibrary.receiver
 * Project: UpdateLibrary
 */
class UpdateReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_UPDATE_START = "com.mml.updatelibrary.service.action.update_start"
        const val ACTION_UPDATE_FAIL = "com.mml.updatelibrary.service.action.update_fail"
        const val ACTION_UPDATE_SUCCESS = "com.mml.updatelibrary.service.action.update_success"
        const val ACTION_UPDATE_RETRY = "com.mml.updatelibrary.service.action.update_retry"
        const val ACTION_UPDATE_PROGRESS = "com.mml.updatelibrary.service.action.update_progress"
        const val ACTION_UPDATE_CANCEL = "com.mml.updatelibrary.service.action.update_cancel"
        const val ACTION_UPDATE_PAUSE = "com.mml.updatelibrary.service.action.update_pause"
        const val ACTION_UPDATE_INSTALL = "com.mml.updatelibrary.service.action.update_install"
        const val NotificationChannelID = "appUpdate"
        const val NOTIFICATION_ID = 1
        @JvmStatic
        fun sendAction(context: Context, action: String, progress: Int? = null) {
            val intent = Intent(action)
            progress?.let {
                intent.putExtra("progress", progress)
            }
            context.sendBroadcast(intent)
        }
    }

    private lateinit var notificationCompatBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    private lateinit var pauseAction: NotificationCompat.Action
    private lateinit var cancelAction: NotificationCompat.Action
    private lateinit var reTryAction: NotificationCompat.Action
    private var mContext: Context = GlobalContextProvider.getGlobalContext()

    init {
        initNotification(mContext)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            when (it.action) {
                ACTION_UPDATE_START -> {
                    log(ACTION_UPDATE_START, tag = "UpdateReceiver")
                    showNotification()
                }
                ACTION_UPDATE_PROGRESS -> {
                    log(ACTION_UPDATE_PROGRESS, tag = "UpdateReceiver")
                    val progress = it.getIntExtra("progress", -1)
                    log("ACTION_UPDATE_PROGRESS:$progress", tag = "UpdateReceiver")
                    updateNotificationProgress(progress)
                }
                ACTION_UPDATE_CANCEL -> {
                    log(ACTION_UPDATE_CANCEL, tag = "UpdateReceiver")
                    cancelNotification()
                }
                ACTION_UPDATE_FAIL -> {
                    log(ACTION_UPDATE_FAIL, tag = "UpdateReceiver")
                    updateNotificationProgressContentToDownLoadFail()
                }
                ACTION_UPDATE_SUCCESS -> {
                    log(ACTION_UPDATE_SUCCESS, tag = "UpdateReceiver")
                    updateNotificationProgressContentToDownLoadSuccess()
                }
                ACTION_UPDATE_RETRY -> {
                    log(ACTION_UPDATE_RETRY, tag = "UpdateReceiver")
                    updateNotificationProgressContentToDownLoadRetry()
                }
                ACTION_UPDATE_INSTALL -> {
                    log(ACTION_UPDATE_INSTALL, tag = "UpdateReceiver")
                    Utils.installApk(
                       mContext,
                        File(context!!.filesDir,"update.apk")
                       // File(context!!.getDir("update", Context.MODE_PRIVATE), "update.apk")
                    )
                }
                ACTION_UPDATE_PAUSE -> {
                    log(ACTION_UPDATE_PAUSE, tag = "UpdateReceiver")
                }
                else -> {
                }
            }
        }
    }

    private fun initNotification(applicationContext: Context) {
        notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationCompatBuilder =
            NotificationCompat.Builder(applicationContext, NotificationChannelID)
                .setContentTitle(
                    applicationContext.getString(
                        R.string.is_updating
                    )
                )
                .setSmallIcon(R.drawable.ic_update_logo)
                .setContentText(applicationContext.getString(R.string.update_progress, "0%"))
                .setProgress(100, 0, false)
                .setColor(-0x1dea20)
                .setTicker("开始下载...")
                .setDefaults(Notification.DEFAULT_LIGHTS) //设置通知的提醒方式： 呼吸灯
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) //设置通知的优先级：
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setSubText("更新下载中")
                .setOnlyAlertOnce(true)
        //  .setOngoing(true)    //true使notification变为ongoing，用户不能手动清除，类似QQ,false或者不设置则为普通的通知

        pauseAction = NotificationCompat.Action(
            R.drawable.ic_noti_action_pause,
            applicationContext.getString(R.string.noti_action_pause),
            getPendingIntent(ACTION_UPDATE_PAUSE)
        )
        cancelAction = NotificationCompat.Action(
            R.drawable.ic_noti_action_cancel,
            applicationContext.getString(R.string.noti_action_cancel),
            getPendingIntent(ACTION_UPDATE_CANCEL)
        )
        reTryAction = NotificationCompat.Action(
            R.drawable.ic_noti_action_cancel,
            applicationContext.getString(R.string.noti_action_retry),
            getPendingIntent(ACTION_UPDATE_RETRY)
        )
    }

    private fun showNotification() {
        log("update     showNotification().", tag = "UpdateReceiver")

        notificationCompatBuilder.addAction(pauseAction)
        notificationCompatBuilder.addAction(cancelAction)
        val pIntent = getPendingIntent(ACTION_UPDATE_INSTALL)
        notificationCompatBuilder.setContentIntent(pIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan1 = NotificationChannel(
                NotificationChannelID,
                mContext.getString(R.string.noti_channel_default),
                NotificationManager.IMPORTANCE_HIGH
            )
            // 是否在桌面icon右上角展示小红点
            chan1.setShowBadge(false)
            chan1.enableLights(true)
            chan1.lightColor = Color.GREEN
            chan1.setShowBadge(true)
            chan1.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            notificationManager.createNotificationChannel(chan1)
        }
    }

    private fun getPendingIntent(type: String): PendingIntent {
        val intent = Intent(type)
        return PendingIntent.getBroadcast(mContext, 0, intent, 0)
    }

    private fun cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun updateNotificationProgress(progress: Int) {
        log("update     updateNotificationProgress()-->$progress.", tag = "UpdateReceiver")
        if (progress < 100) {
            updateNotificationProgressContent(progress)
        } else if (progress >= 100) {
            updateNotificationProgressContentToDownLoadSuccess()
        }
    }

    private fun updateNotificationProgressContentToDownLoadSuccess() {
        log(
            "update     updateNotificationProgressContentToDownLoadSuccess().",
            tag = "UpdateReceiver"
        )
        notificationCompatBuilder.apply {
            removeActions()
            setProgress(0, 0, false)
            setContentTitle(mContext.getString(R.string.update_finish_title))
            setContentText(mContext.getString(R.string.update_finish))
            val pIntent = getPendingIntent(ACTION_UPDATE_INSTALL)
            setContentIntent(pIntent)
        }
        notifyNotification()
    }

    private fun updateNotificationProgressContentToDownLoadFail() {
        notificationCompatBuilder.apply {
            setProgress(0, 0, false)
            setContentTitle(mContext.getString(R.string.update_fail_title))
            setContentText(mContext.getString(R.string.update_fail))
            val pIntent = getPendingIntent(ACTION_UPDATE_RETRY)
            setContentIntent(pIntent)
        }
        notifyNotification()
    }

    private fun updateNotificationProgressContentToDownLoadRetry() {
        notificationCompatBuilder.apply {
            removeActions()
            addAction(reTryAction)
            addAction(cancelAction)
            setContentTitle(
                mContext.getString(
                    R.string.is_updating
                )
            )
        }
        DownloadAppUtil.reTry()
        updateNotificationProgressContent(0)
    }

    private fun updateNotificationProgressContent(progress: Int) {
        log("update     updateNotificationProgressContent().", tag = "UpdateReceiver")
        notificationCompatBuilder.apply {
            removeActions()
            addAction(reTryAction)
            addAction(cancelAction)
            setProgress(100, progress, false)
            setContentText(
                mContext.getString(
                    R.string.update_progress,
                    "${progress}%"
                )
            )
        }
        notifyNotification()
    }

    private fun notifyNotification() {
        log("update     notifyNotification().", tag = "UpdateReceiver")
        notificationManager.notify(NOTIFICATION_ID, notificationCompatBuilder.build())
    }
}