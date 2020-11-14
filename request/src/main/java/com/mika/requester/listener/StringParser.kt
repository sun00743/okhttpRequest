package com.mika.requester.listener

import okhttp3.Response
import java.io.IOException

/**
 * Created by mika on 2018/7/22.
 */
class StringParser : ResponseParser<String> {

    override fun parseNetworkResponse(response: Response): String {
        return response.body?.string() ?: throw IOException("ResponseParser: response body String is null")
    }

}