package com.mika.request.request

import com.mika.request.listener.ResponseListener
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody

/**
 * Created by mika on 2018/9/10.
 * post json request
 */
class PostJsonRequest(url: String, val content: String, listener: ResponseListener<*>) : Requester(url, listener) {

    override fun buildOkHttpRequest(): Request {

        val requestBody = RequestBody.create(
                MediaType.parse("application/json;charset=utf-8"), content)
        requestBuilder.post(requestBody)

        return requestBuilder.build()
    }

}