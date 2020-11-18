package com.mika.demo

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mika.requester.Result
import com.mika.requester.listener.DownloadFileParser
import com.mika.requester.listener.StringParser
import com.mika.requester.request.GetRequester
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }

//        Connector.interceptError = { msg: String?, code: Int? ->
//            false
//        }

        button_request_get.setOnClickListener {
            val url = "https://cn.bing.com/search"
//            requestBingOnScope(url)
            requestBing(url)
        }

        button_request_post.setOnClickListener {
            val file = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val fileParser = DownloadFileParser(file?.absolutePath!!, "image")
            val executeJob = GetRequester("https://cn.bing.com/search", fileParser)
                    .addParam("q", "android")
                    .inProgress { fl: Float, l: Long ->
                        Log.d("mika_file", "thread: ${Thread.currentThread().name}  f: $fl , total: $l")
                    }
                    .success {
                        Log.d("mika_file", it.length().toString())
                    }
                    .error { msg, code ->
                        Log.d("mika_file", "error: \n $msg")
                    }
                    .execute(lifecycleScope)
//            executeJob.cancel()
        }
    }

    fun requestBing(url: String) {
        val startTime = System.currentTimeMillis()

        val job = GetRequester(url, StringParser())
                .addParam("q", "android")
                .success {
                    text_result.text = it

                    val time = System.currentTimeMillis() - startTime
                    Log.d("mika_run_time", time.toString())
                }
                .error { msg, code ->
                    text_result.text = msg

                    val time = System.currentTimeMillis() - startTime
                    Log.d("mika_run_time", time.toString())
                }
                .execute(lifecycleScope)
        lifecycleScope.launch(Dispatchers.IO) {
            delay(75)
            Log.d("mika_cancel", "job cancel invoke")
            job.cancel(CancellationException("mika cancel"))
        }
    }

    fun requestBingOnScope(url: String) {
        val job = lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                GetRequester(url, StringParser())
                        .addParam("q", "android")
//                        .inProgress { progress, length ->
//                            //in IO thread, need post value to main
//                            launch(Dispatchers.Main) {
//                                text_result.text = progress.toString()
//                            }
//                        }
                        .executeOnScope()
            }
            when (result) {
                is Result.Success -> text_result.text = result.value
                is Result.Error ->  text_result.text = result.exception.toString()
            }
        }
//        lifecycleScope.launch {
//            delay(1000)
//            Log.d("mika_run", "job cancel")
//            job.cancelChildren(CancellationException("mika cancel"))
//            job.cancel(CancellationException("mika cancel"))
//        }
    }

}
