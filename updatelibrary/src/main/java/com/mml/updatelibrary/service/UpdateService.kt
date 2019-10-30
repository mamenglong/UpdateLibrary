package com.mml.updatelibrary.service

import android.app.*
import android.content.*
import android.os.IBinder
import com.mml.updatelibrary.receiver.UpdateReceiver
import com.mml.updatelibrary.receiver.UpdateReceiver.Companion.ACTION_UPDATE_CANCEL
import com.mml.updatelibrary.receiver.UpdateReceiver.Companion.ACTION_UPDATE_FAIL
import com.mml.updatelibrary.receiver.UpdateReceiver.Companion.ACTION_UPDATE_INSTALL
import com.mml.updatelibrary.receiver.UpdateReceiver.Companion.ACTION_UPDATE_PAUSE
import com.mml.updatelibrary.receiver.UpdateReceiver.Companion.ACTION_UPDATE_PROGRESS
import com.mml.updatelibrary.receiver.UpdateReceiver.Companion.ACTION_UPDATE_RETRY
import com.mml.updatelibrary.receiver.UpdateReceiver.Companion.ACTION_UPDATE_START
import com.mml.updatelibrary.receiver.UpdateReceiver.Companion.ACTION_UPDATE_SUCCESS
import com.mml.updatelibrary.receiver.UpdateReceiver.Companion.sendAction
import com.mml.updatelibrary.util.log

class UpdateService : Service() {
    companion object {
        @JvmStatic
        fun start(context: Context) {
            context.startService(Intent(context, UpdateService::class.java))
        }
    }

    private val updateReceiver=UpdateReceiver()
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        log("update service is create.", tag = "UpdateService")
        registerReceiver()
        sendAction(applicationContext, ACTION_UPDATE_START)
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("update service   onStartCommand.", tag = "UpdateService")
        return START_STICKY_COMPATIBILITY// super.onStartCommand(intent, flags, startId)

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver()
    }


    private fun registerReceiver() {
        log("update service   registerReceiver().", tag = "UpdateService")
        registerReceiver(updateReceiver, IntentFilter(ACTION_UPDATE_START))
        registerReceiver(updateReceiver, IntentFilter(ACTION_UPDATE_FAIL))
        registerReceiver(updateReceiver, IntentFilter(ACTION_UPDATE_SUCCESS))
        registerReceiver(updateReceiver, IntentFilter(ACTION_UPDATE_RETRY))
        registerReceiver(updateReceiver, IntentFilter(ACTION_UPDATE_PROGRESS))
        registerReceiver(updateReceiver, IntentFilter(ACTION_UPDATE_CANCEL))
        registerReceiver(updateReceiver, IntentFilter(ACTION_UPDATE_PAUSE))
        registerReceiver(updateReceiver, IntentFilter(ACTION_UPDATE_INSTALL))

    }

    private fun unregisterReceiver() {
        log("update service   unregisterReceiver().", tag = "UpdateService")
        unregisterReceiver(updateReceiver)
    }



}
