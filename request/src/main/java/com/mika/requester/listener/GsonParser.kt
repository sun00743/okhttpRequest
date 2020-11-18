package com.mika.requester.listener

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mika.requester.util.ParameterizedTypeImp
import okhttp3.Response
import java.io.IOException
import java.lang.reflect.Type

/**
 * Created by mika on 2018/7/22.
 */
class GsonParser<T>(var rawType: Type? = null, var typeOfRaw: Type? = null) : ResponseParser<T> {

    companion object {
        @JvmStatic
        val GSON = lazy {
            GsonBuilder().create()
        }
    }

    override fun parseNetworkResponse(response: Response): T {
        val stringBody = response.body?.string()
                ?: throw Exception("mika: response body String is null")

        val type = if (rawType != null && typeOfRaw != null) {
            ParameterizedTypeImp(rawType!!, typeOfRaw!!)
        } else {
            object : TypeToken<T>() {}.type
        }
        return GSON.value.fromJson<T>(stringBody, type)
    }

}