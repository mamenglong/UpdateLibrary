# UpdateLibrary
* 使用
    + 本地配置
        ```kotlin
          UpdateUtil.
                setUpdateConfigInfo(UpdateInfo())
                .checkUpdate()
        ```
     + 网络配置
        ```kotlin
          UpdateUtil.
                setUpdateUrl(url)
                .checkUpdate()
        ```
* kotlin使用
    ```kotlin
         UpdateUtil.apply { 
                isDebug=true
                onError={}
                onStartCheck={}
                onFinishCheck={}
                onError={}
                onNoUpdate={}
                setUpdateConfigInfo(UpdateInfo())
                setUpdateUrl("")
                checkUpdate()
            }
    ```
* 配置参考
    ```json
    {
      "updateTitle": "",
      "updateContent": "",
      "apkUrl": "http://dl-ks.coolapkmarket.com/down/apk_upload/2019/1013/2be56956130b91169fd245364df18fe0-245443-o_1dn25652i1p72159lp73n0c1tscq-uid-1463983.apk?t=1571485388&k=a18e3419dd5bf7a67d6f88e869dde6b5",
      "config": {
        "isDebug": false,
        "alwaysShow": false,
        "thisTimeShow": false,
        "force": false,
        "apkSavePath": "",
        "apkSaveName": "",
        "downloadBy": 0,
        "justDownload": false,
        "checkWifi": false,
        "isShowNotification": true,
        "notifyImgRes": 0,
        "needCheckMd5": false,
        "showDownloadingToast": true,
        "serverVersionName": "",
        "serverVersionCode": 2
      }
    }
    
    ```