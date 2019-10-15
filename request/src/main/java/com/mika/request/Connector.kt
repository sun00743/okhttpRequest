package com.mika.request

import com.mika.request.listener.ResponseListener
import okhttp3.*
import java.io.IOException

/**
 * Created by mika on 2018/5/28.
 */
class Connector private constructor() {

    companion object {

        const val REQUEST_NULL_TAG = Byte.MIN_VALUE

        @JvmStatic
        val instance: Connector by lazy { Connector() }

        /**
         * init in application
         */
        @JvmStatic
        fun init(okHttpClient: OkHttpClient?) {
            instance.mDefaultClient = okHttpClient ?: OkHttpClient()
            instance.mPlatform = Platform.get()
        }

    }

    private lateinit var mPlatform: Platform

    /**
     * 一开始初始化的common default client
     */
    private lateinit var mDefaultClient: OkHttpClient

    /**
     * 公共的headers
     */
    private lateinit var mCommonHeaders: HashMap<String, String>

    //-----------------------------------------body------------------------------------------------

    fun setCommonHeaders(headers: HashMap<String, String>) {
        if (!this::mCommonHeaders.isInitialized) {
            mCommonHeaders = HashMap()
        } else {
            mCommonHeaders.clear()
        }
        mCommonHeaders.putAll(headers)
    }

    fun getClient(): OkHttpClient {
        return mDefaultClient
    }

    fun getPlatform(): Platform {
        return mPlatform
    }

    fun newBuilder(): OkHttpClient.Builder {
        return mDefaultClient.newBuilder()
    }

    fun <T> execute(client: OkHttpClient?, request: Request, tag: Any, responseListener: ResponseListener<T>) {
        //callBack
        val okHttpCallback = object : Callback {

            override fun onFailure(call: Call, e: IOException?) {
                postFailureResult(call, e, responseListener, tag)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    if (call.isCanceled) {
                        postCancelResult(call, responseListener, tag)
                    } else {
                        if (responseListener.validateResponse(response, tag)) {
                            val result = responseListener.parseNetworkResponse(response, tag)

                            //TODO check parsed response
                            //check...
                            if (result == null) {
//                                postFailureResult()
                                return
                            }

                            postSuccessResult(result, responseListener, tag)
                        } else {
                            val fMsg = response.message() + " response failure, code: " + response.code() +
                                    " body: " + response.body().toString()
                            postFailureResult(call, IOException(fMsg), responseListener, tag)
                        }
                    }
                } catch (e: Exception) {
                    postFailureResult(call, e, responseListener, tag)
                } finally {
                    response.body()?.close()
                }
            }

        }
        //start call
        responseListener.onStarted(request, tag)
        (client ?: mDefaultClient).newCall(request).enqueue(okHttpCallback)
    }

    private fun <T> postSuccessResult(result: T, responseListener: ResponseListener<in T>, requestTag: Any) {
        mPlatform.execute(Runnable {
            responseListener.onResponse(result, requestTag)
        })
    }

    private fun postFailureResult(call: Call, e: Exception?, listener: ResponseListener<*>, tag: Any) {
        mPlatform.execute(Runnable {
            listener.onFailure(call, e, tag)
        })
    }

    /**
     * post request cancel result
     */
    private fun postCancelResult(call: Call, listener: ResponseListener<*>, tag: Any) {
        mPlatform.execute(Runnable {
            listener.onCanceled(tag)
            listener.onFailure(call, IOException("http Canceled"), tag)
            listener.onFinished(tag)
        })
    }

}