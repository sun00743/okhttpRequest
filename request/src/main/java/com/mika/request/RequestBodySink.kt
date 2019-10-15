package com.mika.request

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*
import java.io.IOException

/**
 * Created by mika on 2018/7/21.
 */
open class RequestBodySink(protected val mBody: RequestBody, protected val mListener: OnProgressListener) : RequestBody() {

    protected lateinit var countingSink: CountingSink

    override fun contentType(): MediaType? {
        return mBody.contentType()
    }

    override fun contentLength(): Long {
        return try {
            mBody.contentLength()
        } catch (e: IOException) {
            e.printStackTrace()
            -1
        }
    }

    override fun writeTo(sink: BufferedSink) {
        countingSink = CountingSink(sink)
        val bufferedSink = Okio.buffer(countingSink)
        mBody.writeTo(bufferedSink)
        bufferedSink.close()
    }


    protected inner class CountingSink(delegate: Sink) : ForwardingSink(delegate) {

        private var bytesWritten = 0L

        override fun write(source: Buffer, byteCount: Long) {
            super.write(source, byteCount)

            bytesWritten += byteCount
            mListener.onRequestProgress(bytesWritten, contentLength())
        }

    }

    interface OnProgressListener {
        fun onRequestProgress(bytesWritten: Long, contentLength: Long)
    }

}