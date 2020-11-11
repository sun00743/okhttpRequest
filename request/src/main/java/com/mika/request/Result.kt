package com.mika.request

import java.lang.Exception

/**
 * Created by mika on 2020/11/11.
 */
sealed class Result<R>{

    data class Success<T>(val value: T) : Result<T>()

    data class Error(val exception: Exception): Result<Nothing>()

}