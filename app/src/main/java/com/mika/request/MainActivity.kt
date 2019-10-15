package com.mika.request

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.mika.request.listener.StringListener
import com.mika.request.request.GetRequester
import com.mika.request.request.PostFromRequester
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.Call
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }

        button_request_get.setOnClickListener {
            val requester = GetRequester("https://cn.bing.com/search", object : StringListener() {

                override fun onFailure(call: okhttp3.Call, e: Exception?, tag: Any) {
                    Log.d("onFailure", e?.message)
                }

                override fun onResponse(response: String, tag: Any) {
                    Log.d("onResponse", response)
                }

            })
            requester.addParam("q", "android")
            requester.execute()
        }

        button_request_post.setOnClickListener {

            val externalPath = Environment.getExternalStorageDirectory().absolutePath
            val file = File(externalPath, "v1.mp4")
            if (file.exists()) {
                val postFromRequester = PostFromRequester("https://cn.bing.com", object : StringListener() {

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
            }

        }
    }
}
