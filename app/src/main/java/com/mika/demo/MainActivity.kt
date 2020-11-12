package com.mika.demo

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mika.requester.Result
import com.mika.requester.request.GetStringRequester
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }

        button_request_get.setOnClickListener {
            requestBing()
        }

        button_request_post.setOnClickListener {
            val externalPath = Environment.getExternalStorageDirectory().absolutePath
            val file = File(externalPath, "v1.mp4")
            if (file.exists()) {
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
            }

        }
    }

    fun requestBing() {
        val url = "https://cn.bing.com/search"
        GetStringRequester(url)
                .addParam("q", "android")
                .execute<String>(lifecycleScope) { result: Result<out String> ->
                    when (result) {
                        is  Result.Success-> {
                            text_result.text = result.value
                        }
                        is  Result.Error-> {
                            text_result.text = result.exception.message
                        }
                    }
                }
    }
}
