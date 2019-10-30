package com.mml.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mml.updatelibrary.util.Utils
import com.mml.updatelibrary.UpdateUtil
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_update.setOnClickListener {
            UpdateUtil.checkUpdate(BuildConfig.VERSION_CODE)
        }
        btn_update_process.setOnClickListener {
          Utils.installApk(this, File(getExternalFilesDir("update")!!,"update.apk"))
        }
        btn_update_Cancel_no_notice.setOnClickListener {
            UpdateUtil.cancelNoLongerRemind()
        }
    }
}
