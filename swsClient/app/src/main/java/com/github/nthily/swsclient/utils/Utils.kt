package com.github.nthily.swsclient.utils

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.ContextWrapper
import android.util.Log

object Utils {

    fun log(str: String) {
        Log.d(TAG, str)
    }

    fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

}
