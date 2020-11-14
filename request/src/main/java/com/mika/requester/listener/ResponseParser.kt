package com.mika.requester.listener

import okhttp3.Call
import okhttp3.Request
import okhttp3.Response
import kotlin.jvm.Throws

/**
 * Created by mika on 2018/6/3.
 */
interface ResponseParser<T> {

//    fun onSuccess(request: T)

    /**
     * if you parse response code in parseNetworkResponse, you should make this method return true.
     */
//    open fun validateResponse(response: Response, tag: Any): Boolean {
//        return response.isSuccessful
//    }

    /**
     * 验证服务器返回信息是否包含session过期提示
     */
/*
    fun validateSession(response: String): Boolean {
        //validate some thing ...
    }
*/

    /**
     * Thread Pool Thread
     */
    @Throws(Exception::class)
    fun parseNetworkResponse(response: Response): T

}