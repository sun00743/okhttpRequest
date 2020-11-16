package com.mika.requester

import android.util.Log
import com.mika.requester.listener.DownloadFileParser
import com.mika.requester.listener.ResponseParser
import com.mika.requester.request.Requester
import kotlinx.coroutines.*
import okhttp3.*
import java.io.File
import java.io.IOException
import java.lang.Runnable
import java.util.*
import kotlin.collections.HashMap
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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

    /**
     * error返回拦截器
     */
    var interceptError: ((errorMsg: String?, errorCode: Int?) -> Boolean)? = null

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

/*
    fun <T> execute(client: OkHttpClient?, requester: Requester<T>) {
        val request = requester.buildOkHttpRequest()
        val okHttpCallback = object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                TODO("Not yet implemented")
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val result = requester.parser.parseNetworkResponse(response)
                    response.body?.close()
                    mPlatform.execute(Runnable {
                        requester.successBlock?.invoke(result)
                    })
                } catch (e: Exception) {
//                    postFailureResult(call, e, responseListener, tag)
                } finally {
//                    response.body()?.close()
                }
            }

        }
        //start callm
        (client ?: mDefaultClient).newCall(request).enqueue(okHttpCallback)
    }
*/

    fun <T> execute(client: OkHttpClient?, requester: Requester<T>, coroutineScope: CoroutineScope): Job {
        val request = requester.buildOkHttpRequest()
        return coroutineScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val response = (client ?: mDefaultClient).newCall(request).execute()
                    if (response.isSuccessful) {
                        val parser = requester.parser
                        //down load file
                        if (parser is DownloadFileParser) {
                            parser.coroutineScope = this@launch
                            parser.progressBlock = requester.progressBlock
                        }
                        val value = parser.parseNetworkResponse(response)
                        response.body?.close()
                        Result.Success(value)
                    } else {
                        Result.Error(Exception("http response error, msg: ${response.message}"), response.code)
                    }
                } catch (e: Exception) {
                    Result.Error(e)
                }
            }
            when (result) {
                is Result.Success -> requester.successBlock?.invoke(result.value as T)
                is Result.Error -> {
                    if (interceptError?.invoke(result.exception.message, result.code) == true) {
                        return@launch
                    }
                    requester.errorBlock?.invoke(result.exception.message!!, result.code!!)
                }
            }
        }
    }

    suspend fun <T> executeOnScope(client: OkHttpClient?, requester: Requester<T>) = suspendCancellableCoroutine<Result<out T>> {
        val request = requester.buildOkHttpRequest()

        val result = try {
            val response = (client ?: mDefaultClient).newCall(request).execute()
            if (response.isSuccessful) {
                val parser = requester.parser

                //down load file
                if (parser is DownloadFileParser) {
//                    parser.progressData?.observeForever(requester.progressDataObserve!!)
//                    parser.coroutineScope = this
//                    parser.progressBlock = requester.progressBlock
                }
                //parse response
                val value = parser.parseNetworkResponse(response)
                response.body?.close()

//                if (parser is DownloadFileParser) {
//                    parser.progressData?.removeObserver(requester.progressDataObserve!!)
//                    parser.progressData = null
//                }
                Result.Success(value)
            } else {
                Result.Error(Exception("http response error, msg: ${response.message}"), response.code)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }

        it.resume(result)
    }


}