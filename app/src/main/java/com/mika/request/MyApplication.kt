package com.mika.request

import android.app.Application
import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Created by mika on 2018/7/22.
 */
class MyApplication: Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()

        initOkHttpClient()
    }


    private fun initOkHttpClient() {
        val okHttpClient = OkHttpClient.Builder()
                //                .addInterceptor(new LoggerInterceptor("TAG"))
                .connectTimeout(15000L, TimeUnit.MILLISECONDS)    //15s time out
                .readTimeout(15000L, TimeUnit.MILLISECONDS)       //15s read out
                .retryOnConnectionFailure(false) //错误重连
                .addInterceptor(HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY)
                )
                //其他配置
                //                .authenticator()
                //                .cookieJar()
                //                .sslSocketFactory()  //ssl
                .build()
        Connector.init(okHttpClient)
//        Connector.instance.setCommonHeaders();
    }

}