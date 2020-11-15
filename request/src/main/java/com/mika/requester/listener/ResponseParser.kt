package com.mika.requester.listener

import okhttp3.Call
import okhttp3.Request
import okhttp3.Response
import kotlin.jvm.Throws

/**
 * Created by mika on 2018/6/3.
 */
interface ResponseParser<T> {

    /**
     * Thread Pool Thread
     */
    @Throws(Exception::class)
    fun parseNetworkResponse(response: Response): T

}