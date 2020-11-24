# okhttpRequest
okhttp with coroutines. 

## 说明
在build.gradle中引入 
```
implementation 'com.github.sun00743.okhttpRequest:request:1.0.1'
```

使用kotlin提供的协程库以及okhttp，简单封装了一下，让协程（Dispatchers.IO）来负责请求网络及线程转换。

使用gson来提供json format。


## 使用
1.初始化，首先在Application或者什么地方先初始化，像使用okhttp一样，构建okhttpclient，然后把Client当作参数传入Connector
```
        val okHttpClient = OkHttpClient.Builder()
                //...
                .build()
        Connector.init(okHttpClient)
```
2.发送请求
execute 方法传递CoroutineScope进去。官方提供lifecycleScope是不错的选择，毕竟通常我们关掉Activity的同时也想干掉请求。
```
GetRequester(url, StringParser())
                .success {
                    text_result.text = it
                }
                .error { msg, code ->
                    text_result.text = msg
                }
                .execute(lifecycleScope)
```

待续...
