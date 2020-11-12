package com.mika.requester

import com.mika.requester.request.Requester
import kotlinx.coroutines.*
import okhttp3.*

/**
 * Created by mika on 2018/5/28.
 */
object Connector {

    const val REQUEST_NULL_TAG = Byte.MIN_VALUE

    private lateinit var mPlatform: Platform

    /**
     * 一开始初始化的common default client
     */
    private lateinit var mDefaultClient: OkHttpClient

    /**
     * 公共的headers
     */
    private lateinit var mCommonHeaders: HashMap<String, String>

    @JvmStatic
    fun init(okHttpClient: OkHttpClient?) {
        mDefaultClient = okHttpClient ?: OkHttpClient()
        mPlatform = Platform.get()
    }

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

    fun <T> execute(client: OkHttpClient?, requester: Requester<T>, coroutineScope: CoroutineScope,
                    block: (result: Result<out T>) -> Unit) {
        val request = requester.buildOkHttpRequest()
        coroutineScope.launch {
            val withContext = withContext(Dispatchers.IO) {
                try {
                    val response = (client ?: mDefaultClient).newCall(request).execute()
                    if (response.isSuccessful) {
                        Result.Success(requester.parseNetworkResponse(response))
                    } else {
                        Result.Error(Exception("http response error, code: ${response.code}, msg: ${response.message}"))
                    }
                } catch (e: Exception) {
                    Result.Error(e)
                }
            }
            block.invoke(withContext)
        }
    }


}