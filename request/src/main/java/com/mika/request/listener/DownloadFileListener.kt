package com.mika.request.listener

import okhttp3.Call
import okhttp3.Response
import java.io.File

/**
 * Created by mika on 2018/9/28.
 */
abstract class DownloadFileListener: ResponseListener<File>() {

    private lateinit var fileDir: String

    private lateinit var fileName: String


    override fun parseNetworkResponse(response: Response, tag: Any): File {
        //todo mika
        
    }

}