package com.mika.requester.listener

import okhttp3.Response
import java.io.IOException

/**
 * Created by mika on 2018/7/22.
 */
abstract class StringListener : ResponseListener<String>() {

    override fun parseNetworkResponse(response: Response, tag: Any): String {
        val stringResult = response.body?.string() ?: throw IOException("response body is null")
        //can validate result here
        //validateResponse(stringResult)
        return stringResult
    }
}