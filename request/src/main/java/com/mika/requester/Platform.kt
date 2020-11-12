package com.mika.requester

import android.os.Build
import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Created by mika on 2018/5/27.
 */
open class Platform {

    companion object {

        private val platform = findPlatform()

        private fun findPlatform(): Platform {
            Class.forName("android.os.Build")
            if (Build.VERSION.SDK_INT != 0) {
                return Android()
            }
            return Platform()
        }

        fun get(): Platform {
            return platform
        }

    }

    open fun defaultCallbackExecutor(): Executor {
        return Executors.newCachedThreadPool()
    }

    open fun execute(runnable: Runnable) {
        defaultCallbackExecutor().execute(runnable)
    }

    class Android : Platform() {

        override fun defaultCallbackExecutor(): Executor {
            return MainThreadExecutor()
        }

        inner class MainThreadExecutor : Executor {

            private val handler = Handler(Looper.getMainLooper())

            override fun execute(command: Runnable) {
                handler.post(command)
            }
        }

    }

}