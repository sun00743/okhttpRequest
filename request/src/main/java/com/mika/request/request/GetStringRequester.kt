package com.mika.request.request

import okhttp3.Response
import java.lang.Exception

/**
 * Created by mika on 2020/11/12.
 */
class GetStringRequester(url: String): GetRequester<String>(url, null) {

    override fun parseNetworkResponse(response: Response): String {
        return response.body?.string() ?: throw Exception("mika: response body String is null")
    }

}