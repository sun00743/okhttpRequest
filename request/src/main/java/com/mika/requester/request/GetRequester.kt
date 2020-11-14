package com.mika.requester.request

import android.net.Uri
import com.mika.requester.listener.ResponseParser
import okhttp3.Request

/**
 * Created by mika on 2018/6/9.
 */
class GetRequester<T>(url: String, parser: ResponseParser<T>) : Requester<T>(url, parser) {

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

    fun addParam(key: String, value: String): GetRequester<T> {
        if (!isParamsMapInit()) {
            paramsMap = linkedMapOf(key to value)
        }
        paramsMap[key] = value
        return this
    }

    fun addParamMap(paramMap: HashMap<String, String>) {
        paramsMap = paramMap
    }

}