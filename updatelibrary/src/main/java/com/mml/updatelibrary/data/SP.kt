package com.mml.updatelibrary.data

import com.mml.easyconfig.config.Config

/**
 * Author: Menglong Ma
 * Email: mml2015@126.com
 * Date: 19-10-11 下午5:59
 * Description: This is SP
 * Package: com.mml.updatelibrary.data
 * Project: OnceApplication
 */
object SP: Config() {
    override val isEncode: Boolean
        get() = true
    override val spName: String
        get() =  "updateConfig"

    var ignoreVersion:Int by delegate.int(0)
}