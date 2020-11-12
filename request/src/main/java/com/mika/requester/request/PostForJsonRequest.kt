package com.mika.requester.request

import com.google.gson.GsonBuilder
import com.mika.requester.listener.ResponseListener
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

/**
 * Created by mika on 2018/9/10.
 * post json request
 */
class PostForJsonRequest<T>(url: String, val content: String, listener: ResponseListener<*>) : Requester<T>(url, listener) {

    var resultCls: Class<T>? = null

    override fun buildOkHttpRequest(): Request {
        val requestBody = content.toRequestBody("application/json;charset=utf-8".toMediaTypeOrNull())
        requestBuilder.post(requestBody)
        return requestBuilder.build()
    }

    override fun parseNetworkResponse(response: Response): T {
        val stringBody = response.body?.string()
                ?: throw Exception("mika: response body String is null")

        return GSON.value.fromJson<T>(stringBody, resultCls)
    }

    companion object {
        val GSON = lazy {
            GsonBuilder().create()
        }
    }

}