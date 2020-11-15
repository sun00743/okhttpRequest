package com.mika.requester.listener

import com.mika.requester.Connector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.Response
import okio.buffer
import okio.sink
import okio.source
import java.io.File

/**
 * Created by mika on 2018/9/28.
 */
class DownloadFileParser(var fileDir: String, var fileName: String) : ResponseParser<File> {

    var coroutineScope: CoroutineScope? = null

    internal var progressBlock: ((progress: Float, length: Long) -> Unit)? = null

    override fun parseNetworkResponse(response: Response): File {
        response.body?.use { body ->

            val length = body.contentLength()
            val inputStream = body.byteStream()
            val buf = ByteArray(2048)
            var sum = 0L
            var len = 0

            val fileDir = File(fileDir)
            if (!fileDir.exists()) {
                fileDir.mkdirs()
            }

            val isb = inputStream.source().buffer()
            val result = File(fileDir, fileName)
            val osb = result.sink().buffer()

            try {
                while (len != -1) {
                    len = isb.read(buf)
                    sum += len
                    osb.write(buf, 0, len)
                    val progress = sum * 1.0f / length
                    coroutineScope?.launch {
                        progressBlock?.invoke(progress, length)
                    }
//                    Connector.getPlatform().execute(Runnable {
//                    })
                }
                osb.flush()
            } finally {
                isb.close()
                osb.close()
            }
            return result
        }

        throw Exception("parse file failed")
    }

}