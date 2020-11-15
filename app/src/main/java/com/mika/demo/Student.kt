package com.mika.demo

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by mika on 2020/11/15.
 */
@Parcelize
data class Student(val name: String, val age: Int, val sex: Int) : Parcelable