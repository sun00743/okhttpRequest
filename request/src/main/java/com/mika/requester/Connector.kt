package com.mika.requester

import android.util.Log
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
     * error返回拦截器，拦截以后[Requester.error]不会回调
     *
     * 哪个[Job]执行execute方法，便在在它的[CoroutineDispatcher]中执行拦截方法
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
//        val request = requester.buildOkHttpRequest()
        return coroutineScope.launch {
            val result = withContext(Dispatchers.IO) {
                executeOnScope(client, requester, this@launch, false)
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

    suspend fun <T> executeOnScope(client: OkHttpClient?, requester: Requester<T>, progressScope: CoroutineScope? = null,
                                   needIntercept: Boolean? = true) = suspendCancellableCoroutine<Result<out T>> { uCont ->
        val request = requester.buildOkHttpRequest()
        val useClient = client ?: mDefaultClient
        val result = try {
            useClient.newCall(request).run {
                //job cancel listener
                uCont.invokeOnCancellation {
                    //cancel okHttp call
                    cancel()
                }
                execute().use { response ->
                    if (isCanceled()) {
                        val e = Exception("http request cancel, msg: ${response.message}")
                        return@run Result.Error(e, response.code)
                    }
                    if (response.isSuccessful) {
                        val parser = requester.parser
                        //set post down load file progress
                        if (parser is DownloadFileParser) {
                            if (progressScope != null) {
                                postInProgress(parser, progressScope, requester.progressBlock)
                            } else {
                                parser.progressListener = requester.progressBlock
                            }
                        }
                        //parse response
                        val value = parser.parseNetworkResponse(response)
                        Result.Success(value)
                    } else {
                        val e = Exception("http response error, msg: ${response.message}")
                        Result.Error(e, response.code)
                    }
                }
            }
        } catch (e: Exception) {
            Result.Error(e)
        }

        if (needIntercept != false && result is Result.Error) {
            MainScope().launch {
                if (interceptError?.invoke(result.exception.message, result.code) == true) {
                    uCont.cancel()
                }
            }
        }
        uCont.resume(result)
    }

    private fun postInProgress(parser: DownloadFileParser, coroutineScope: CoroutineScope,
                               progressBlock: ((progress: Float, length: Long) -> Unit)?) {
        parser.progressListener = { progress: Float, length: Long ->
            coroutineScope.launch {
                progressBlock?.invoke(progress, length)
            }
        }
    }


}