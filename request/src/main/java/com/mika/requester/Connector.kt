package com.mika.requester

import com.mika.requester.listener.DownloadFileParser
import com.mika.requester.request.Requester
import kotlinx.coroutines.*
import okhttp3.*
import kotlin.collections.HashMap
import kotlin.coroutines.resume

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
                            postInProgress(parser, this@launch, requester.progressBlock)
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
                is Result.Success -> requester.successBlock?.invoke(result.value)
                is Result.Error -> {
                    if (interceptError?.invoke(result.exception.message, result.code) == true) {
                        return@launch
                    }
                    requester.errorBlock?.invoke(result.exception.message!!, result.code!!)
                }
            }
        }
    }

    private fun postInProgress(parser: DownloadFileParser, coroutineScope: CoroutineScope,
                               progressBlock: ((progress: Float, length: Long) -> Unit)?) {
        parser.progressListener = { progress: Float, length: Long ->
            coroutineScope.launch {
                progressBlock?.invoke(progress, length)
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
                    parser.progressListener = requester.progressBlock
                }

                //parse response
                val value = parser.parseNetworkResponse(response)
                response.body?.close()
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