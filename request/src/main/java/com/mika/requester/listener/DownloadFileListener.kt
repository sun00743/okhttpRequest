package com.mika.requester.listener

import okhttp3.Response
import java.io.File
import java.io.FileOutputStream

/**
 * Created by mika on 2018/9/28.
 */
class DownloadFileListener: ResponseParser<File> {

    private lateinit var fileDir: String

    private lateinit var fileName: String

    override fun parseNetworkResponse(response: Response): File {
        //todo mika
        val contentLength = response.body?.contentLength()
        response.body?.byteStream()?.let {
            FileOutputStream("").write(it.readBytes())
        }

        return File("")
    }

}