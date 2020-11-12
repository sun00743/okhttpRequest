package com.mika.requester.request

import com.mika.requester.Connector
import com.mika.requester.Result
import com.mika.requester.listener.ResponseListener
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.TimeUnit


/**
 * Created by mika on 2018/6/3.
 */
abstract class Requester<T>(protected val url: String, protected val listener: ResponseListener<*>?) {


    private lateinit var mClientBuilder: OkHttpClient.Builder
    protected lateinit var paramsMap: HashMap<String, String>

    /**
     * 如果有其他配置变化，则重新build一个client使用。
     * 例如 readTime 变化等
     */
    protected var useDefaultClient = true
    protected var tag: Any? = null

    /**
     * http request builder
     */
    protected val requestBuilder = Request.Builder()

    init {
        requestBuilder.url(url)
    }

    fun <U> execute(coroutineScope: CoroutineScope, block: (result: Result<out T>) -> Unit) {

//        val body = RequestBody.create(JSON, json)
//        val request = Request.Builder()
//                .url(url)
//                .post(body)
//                .build()

        //todo buildRequestBody wrapRequestBody
        //todo onBefore
        val client: OkHttpClient? = if (useDefaultClient) null else mClientBuilder.build()
        Connector.execute(client, this, coroutineScope, block)
    }

    /**
     * 解析Response
     */
    abstract fun parseNetworkResponse(response: Response): T

    /**
     * requestBuilder build okHttp request
     */
    abstract fun buildOkHttpRequest(): Request

    fun addHeader(key: String, value: String) {
        requestBuilder.addHeader(key, value)
    }

    fun setHeader(key: String, value: String) {
        requestBuilder.header(key, value)
    }

    fun setConnectTimeout(timeMills: Long) {
        getClientBuilder().connectTimeout(timeMills, TimeUnit.MILLISECONDS)
    }

    protected fun getClientBuilder(): OkHttpClient.Builder {
        if (useDefaultClient) {
            useDefaultClient = false
            mClientBuilder = Connector.newBuilder()
        }
        return mClientBuilder
    }

    protected fun isParamsMapInit(): Boolean {
        return this::paramsMap.isInitialized
    }

}