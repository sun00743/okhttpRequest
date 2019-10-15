package com.mika.request.request

import android.net.Uri
import com.mika.request.listener.ResponseListener
import okhttp3.Request

/**
 * Created by mika on 2018/6/9.
 */
class GetRequester(url: String, listener: ResponseListener<*>?) : Requester(url, listener) {

    override fun buildOkHttpRequest(): Request {
        if (isParamsMapInit()) {
            val builder = Uri.parse(url).buildUpon()
            paramsMap.keys.forEach {
                builder.appendQueryParameter(it, paramsMap[it])
            }
            //rebuild url and set in requestBuilder
            requestBuilder.url(builder.build().toString())
        }
        return requestBuilder.build()
    }

    fun addParam(key: String, value: String) {
        if (!isParamsMapInit()) {
            paramsMap = linkedMapOf(key to value)
        }
        paramsMap[key] = value
    }

    fun addParamMap(paramMap: HashMap<String, String>) {
        paramsMap = paramMap
    }
}