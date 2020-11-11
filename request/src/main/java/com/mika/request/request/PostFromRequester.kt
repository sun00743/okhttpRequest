package com.mika.request.request

import com.mika.request.Connector
import com.mika.request.RequestBodySink
import com.mika.request.listener.ResponseListener
import okhttp3.*
import okhttp3.Headers.Companion.headersOf
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.net.URLConnection

/**
 * Created by mika on 2018/6/24.
 * a request post string from only
 */
abstract class PostFromRequester<T>(url: String, listener: ResponseListener<*>?) : Requester<T>(url, listener) {

    private lateinit var files: MutableMap<String, File>

    override fun buildOkHttpRequest(): Request {
        requestBuilder.post(RequestBodyFactory().createRequestBody())
        return requestBuilder.build()
    }

    fun addBody(key: String, value: String) {
        if (!isParamsMapInit()) {
            paramsMap = linkedMapOf(key to value)
        }
        paramsMap[key] = value
    }

    fun addFile(file: File) {
        addFile(file.name, file)
    }

    fun addFile(key: String, file: File) {
        if (!isFilesMapInitialized()) {
            files = mutableMapOf()
        }
        files[key] = file
    }

    private fun isFilesMapInitialized(): Boolean {
        return this::files.isInitialized
    }

    /**
     * the okHttp RequestBody factory
     */
    private inner class RequestBodyFactory {

        fun createRequestBody(): RequestBody {

            return when {
                !isFilesMapInitialized() -> buildStringBody()
                isParamsMapInit() -> buildMultiBody()
                files.size > 1 -> buildMultiFileBody()
                files.size == 1 -> buildFileBody()
                else -> buildStringBody()
            }

/*
            when {
                isFilesMapInitialized() && isParamsMapInit() -> { //multi from
                    return buildMultiBody()
                }
                isFilesMapInitialized() && files.size > 1 -> { //only files from
                    return buildMultiFileBody()
                }
                isFilesMapInitialized() && files.size == 1 -> {
                    return buildFileBody()
                }
                isParamsMapInit() -> { //only string from
                    return buildStringBody()
                }
                else -> {
                    return buildStringBody()
                }
            }
*/
        }

        /**
         * return string from body
         */
        private fun buildStringBody(): RequestBody {
            val formBodyBuilder = FormBody.Builder()
            paramsMap.forEach {
                formBodyBuilder.add(it.key, it.value)
            }
            return formBodyBuilder.build()
        }

        /**
         * return file body
         */
        private fun buildFileBody(): RequestBody {
            val file = files.values.single()
            val requestBody = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            return wrapBody(requestBody)
        }

        /**
         * @return  multi body with files
         */
        private fun buildMultiFileBody(): RequestBody {
            val builder = MultipartBody.Builder()

            addFilesDataPart(builder)
            return wrapBody(builder.build())
        }

        /**
         * @return  multi body with files and strings
         */
        private fun buildMultiBody(): RequestBody {
            val builder = MultipartBody.Builder()
            //add string part
            paramsMap.forEach { item ->
                builder.addPart(headersOf("Content-Disposition", "form-data; name=\"" + item.key + "\""),
                        item.value.toRequestBody(null))
            }

            addFilesDataPart(builder)
            return wrapBody(builder.build())
        }

        /**
         * add file from data part
         */
        private fun addFilesDataPart(builder: MultipartBody.Builder) {
            files.forEach { item ->
                val file = item.value
                val contentType = URLConnection.getFileNameMap().getContentTypeFor(file.name)
                        ?: "application/octet-stream"
                val body = file.asRequestBody(contentType.toMediaTypeOrNull())
                builder.addFormDataPart(item.key, file.name, body)
            }
        }

        private fun wrapBody(requestBody: RequestBody): RequestBody {
            return RequestBodySink(requestBody, object : RequestBodySink.OnProgressListener {

                override fun onRequestProgress(bytesWritten: Long, contentLength: Long) {
                    Connector.getPlatform().defaultCallbackExecutor().execute {
                        listener?.inProgress(
                                (bytesWritten / contentLength).toFloat(),
                                contentLength,
                                tag ?: Connector.REQUEST_NULL_TAG
                        )
                    }
                }

            })
        }

    }

}