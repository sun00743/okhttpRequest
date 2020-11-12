package com.mika.requester.listener

import okhttp3.Call
import okhttp3.Request
import okhttp3.Response
import kotlin.jvm.Throws

/**
 * Created by mika on 2018/6/3.
 */
abstract class ResponseListener<T> {

    open fun onStarted(request: Request, tag: Any) {}

    open fun onCanceled(tag: Any) {}

    open fun onFinished(tag: Any) {}

    /**
     * @param progress  0 ~ 1f
     */
    open fun inProgress(progress: Float, total: Long, tag: Any) {}

    /**
     * if you parse response code in parseNetworkResponse, you should make this method return true.
     */
    open fun validateResponse(response: Response, tag: Any): Boolean {
        return response.isSuccessful
    }

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
    abstract fun parseNetworkResponse(response: Response, tag: Any): T?

    abstract fun onFailure(call: Call, e: Exception?, tag: Any)

    abstract fun onResponse(response: T, tag: Any)

    /**
     * default listener impl
     */
    companion object {

        var responseListener: ResponseListener<*> = object : ResponseListener<Any>() {

            override fun parseNetworkResponse(response: Response, tag: Any): Any {
                return Any()
            }

            override fun onFailure(call: Call, e: Exception?, tag: Any) {
            }

            override fun onResponse(response: Any, tag: Any) {
            }

        }

    }

}