package com.mml.updatelibrary.data

import com.mml.updatelibrary.GlobalContextProvider
import com.mml.updatelibrary.R

data class UpdateInfo(
    // 更新标题
    var updateTitle: String = "新的版更新",//GlobalContextProvider.getGlobalContext().getString(R.string.update_title),
    // 更新内容
    var updateContent: String ="有新的更新",// GlobalContextProvider.getGlobalContext().getString(R.string.update_content),
    // apk 下载地址
    var apkUrl: String = "http://oss.pgyer.com/1bacaeafcca4c3b6a0039265e7d6ca1a.apk?auth_key=1572250908-fa0fefce0efc0d72fca9adba4d4ce785-0-2fe332346ee1131d5b08f2d8ba86fab0&response-content-disposition=attachment%3B+filename%3Delf-4.9.5-495000_jiagu_aligned_signed_test.apk",
    // 更新配置
    var config: UpdateConfig = UpdateConfig()
)


