package com.mml.demo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mml.updatelibrary.service.UpdateService
import com.mml.updatelibrary.ui.UpdateUtil
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_update.setOnClickListener {
            UpdateUtil.checkUpdate()
        }
        btn_update_process.setOnClickListener {
            sendBroadcast(Intent(UpdateService.ACTION_UPDATE_PROGRESS))

        }
        btn_update_Cancel_no_notice.setOnClickListener {
            UpdateUtil.cancelNoLongerRemind()
        }
    }
}
