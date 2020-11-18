package com.mika.requester.listener

import okhttp3.Response
import okio.buffer
import okio.sink
import okio.source
import java.io.File

/**
 * Created by mika on 2018/9/28.
 */
class DownloadFileParser(var fileDir: String, var fileName: String) : ResponseParser<File> {

    var progressListener: ((Float, Long) -> Unit)? = null

    override fun parseNetworkResponse(response: Response): File {
        response.body?.apply {
            val length = contentLength()
            val inputStream = byteStream()
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
                var postTime = 0L
                while (len != -1) {
                    len = isb.read(buf)
                    if (len == -1) break
                    sum += len
                    osb.write(buf, 0, len)
                    var progress = sum * 1.0f / length
                    if (progress > 1.0f) {
                        progress = 0.0f
                    }
                    val currentTimeMillis = System.currentTimeMillis()
                    if (currentTimeMillis - postTime >= 300 || progress == 1.0f) {
                        postTime = currentTimeMillis
                        progressListener?.invoke(progress, length)
                    }
//                    Connector.getPlatform().execute(Runnable {
//                    })
                }
                osb.flush()
            } finally {
                isb.close()
                osb.close()
                progressListener = null
            }
            return result
        }

        throw Exception("parse file failed")
    }


/*
    private fun testParseFile(): File {
        Log.d("mika_file", "start test on thread: ${Thread.currentThread().name}")
        val length = file!!.length()
        val inputStream = FileInputStream(file)
        val buf = ByteArray(2048)
        var sum = 0L
        var len = 0

        val fileDir = File(fileDir)
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }

        val isb = inputStream.source().buffer()
        val result = File(fileDir, fileName)
        if (result.exists()) {
            result.delete()
        }
        val osb = result.sink().buffer()

        try {
            while (len != -1) {
                len = isb.read(buf)
                if (len == -1) break
                sum += len
                osb.write(buf, 0, len)
                val progress = sum * 1.0f / length
                coroutineScope?.launch {
                    progressBlock?.invoke(progress, length)
                }
//                Connector.getPlatform().execute(Runnable {
//                    progressBlock?.invoke(progress, length)
//                })
            }
            osb.flush()
        } finally {
            isb.close()
            osb.close()
        }
        return result
    }
*/


}
