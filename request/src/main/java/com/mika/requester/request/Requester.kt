package com.mika.requester.request

import com.mika.requester.Connector
import com.mika.requester.Result
import com.mika.requester.listener.ResponseParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit


/**
 * Created by mika on 2018/6/3.
 */
abstract class Requester<T>(protected val url: String, val parser: ResponseParser<T>) {


    private lateinit var mClientBuilder: OkHttpClient.Builder
    protected lateinit var paramsMap: HashMap<String, String>

    /**
     * 如果有其他配置变化，则重新build一个client使用。
     * 例如 readTime 变化等
     */
    protected var useDefaultClient = true
    protected var tag: Any? = null

    var errorBlock: ((msg: String, code: Int) -> Unit)? = null
        private set

    var successBlock: ((result: T) -> Unit)? = null
        private set

    var progressBlock: ((progress: Float, length: Long) -> Unit)? = null
        private set


    /**
     * http request builder
     */
    protected val requestBuilder = Request.Builder()

    init {
        requestBuilder.url(url)
    }

/*
    fun execute(coroutineScope: CoroutineScope, block: (result: Result<out T>) -> Unit) {

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
*/

    fun execute(coroutineScope: CoroutineScope): Job {
        //todo buildRequestBody wrapRequestBody
        val client: OkHttpClient? = if (useDefaultClient) null else mClientBuilder.build()
//        return Connector.execute(client, this, successBlock, errorBlock, coroutineScope)
        return Connector.execute(client, this, coroutineScope)
    }

    suspend fun executeOnScope(): Result<out T> {
        //todo buildRequestBody wrapRequestBody
        val client: OkHttpClient? = if (useDefaultClient) null else mClientBuilder.build()
        return Connector.executeOnScope(client, this)
    }

    fun success(successBlock: ((result: T) -> Unit)): Requester<T> {
        this.successBlock = successBlock
        return this
    }

    fun error(errorBlock: ((msg: String, code: Int) -> Unit)): Requester<T> {
        this.errorBlock = errorBlock
        return this
    }

    /**
     * load file progress.
     *
     * when use executeOnScope, this block will running in launch Thread, so may be not in main Thread,
     * you may post value to the main to show.
     */
    fun inProgress(progressBlock: ((progress: Float, length: Long) -> Unit)): Requester<T> {
        this.progressBlock = progressBlock
        return this
    }


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