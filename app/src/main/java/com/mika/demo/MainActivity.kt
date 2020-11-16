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
import com.mika.requester.listener.GsonParser
import com.mika.requester.listener.StringParser
import com.mika.requester.request.GetRequester
import com.mika.requester.util.ParameterizedTypeImp
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.log

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
            requestBing()
        }

        button_request_post.setOnClickListener {

            val file = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val tFile = File(file, "img.jpg")

            Log.d("mika_file", tFile.length().toString())

            val fileParser = DownloadFileParser(file?.absolutePath!!, "image")
//            fileParser.file = tFile
            val executeJob = GetRequester("https://cn.bing.com/search", fileParser)
                    .addParam("q", "android")
                    .inProgress { fl: Float, l: Long ->
                        Log.d("mika_file", "thread: ${Thread.currentThread().name}  f: $fl , total: $l")
                    }
                    .success {
                        Log.d("mika_file", it.length().toString())
//                        text_result.text = it
//                        val time = System.currentTimeMillis() - startTime
//                        Log.d("mika_run_time", time.toString())
                    }
                    .error { msg, code ->
                        Log.d("mika_file", "error: \n $msg")
                    }
                    .execute(lifecycleScope)


//            DownloadFileParser().testParseFile()

//            val json = "[{\"name\":\"a\",\"age\":12,\"sex\":0},{\"name\":\"tom\",\"age\":18,\"sex\":0},{\"name\":\"marry\",\"age\":16,\"sex\":1}]"
//            val parseJson = GsonParser<ArrayList<Student>>(ArrayList::class.java, Student::class.java)
//            Log.d("mika_entity", parseJson.toString())

//            val externalPath = Environment.getExternalStorageDirectory().absolutePath


//            val file = File(externalPath, "v1.mp4")
//            if (file.exists()) {
/*
                val postFromRequester = PostForJsonRequest("https://cn.bing.com", object : StringListener() {

                    override fun inProgress(progress: Float, total: Long, tag: Any) {
                        Log.d("mika inProgress", "progress: " + progress + "total: " + total)
                    }

                    override fun onFailure(call: Call, e: Exception?, tag: Any) {
                        Log.d("mika onFailure", e?.message)
                    }

                    override fun onResponse(response: String, tag: Any) {
                        Log.d("mika onResponse", response)
                    }

                })
                postFromRequester.addFile(file)
                postFromRequester.execute()
*/
//            }

        }
    }

    fun requestBing() {
        val url = "https://cn.bing.com/search"

        val startTime = System.currentTimeMillis()
//        val parseJson = GsonParser<ArrayList<Student>>(ArrayList::class.java, Student::class.java)
//        DownloadFileParser("fileDir", "fileName", )
        val executeJob = GetRequester(url, StringParser())
                .addParam("q", "android")
                .success {
                    text_result.text = it
                    val time = System.currentTimeMillis() - startTime
                    Log.d("mika_run_time", time.toString())
                }
                .error { msg, code ->

                }
//                .execute(lifecycleScope)
//        executeJob.cancel()

        //on cour scope
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                GetRequester(url, StringParser())
                        .addParam("q", "android")
                        .executeOnScope()
            }
            when (result) {
                is Result.Success -> text_result.text = result.value
                is Result.Error -> {//show error ui}
                }
            }
        }
        //end
    }

}
