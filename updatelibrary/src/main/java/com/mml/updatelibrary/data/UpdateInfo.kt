package com.mml.updatelibrary.data

import com.mml.updatelibrary.GlobalContextProvider
import com.mml.updatelibrary.R

data class UpdateInfo(
    // 更新标题
    var updateTitle: String = "",//GlobalContextProvider.getGlobalContext().getString(R.string.update_title),
    // 更新内容
    var updateContent: String ="",// GlobalContextProvider.getGlobalContext().getString(R.string.update_content),
    // apk 下载地址
    var apkUrl: String = "",
    // 更新配置
    var config: UpdateConfig = UpdateConfig()
)


