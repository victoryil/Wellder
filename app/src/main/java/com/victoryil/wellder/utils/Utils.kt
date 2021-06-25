package com.victoryil.wellder.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import com.victoryil.wellder.instances.ServiceManager
import java.util.*


fun debug(text: String) = Log.d("pollo", text)

fun formattedTimeWithParams(date: Date): String {
    val calendar = Calendar.getInstance()
    calendar.time = date
    return "${
        calendar
            .get(Calendar.HOUR_OF_DAY)
    }:${
        if (calendar.get(Calendar.MINUTE) < 10)
            "0${calendar.get(Calendar.MINUTE)}"
        else calendar.get(Calendar.MINUTE).toString()
    }"
}

fun isNetworkAvailable(activity: Activity): Boolean {
    val connectivityManager =
        ServiceManager.getSystemService(
            activity,
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager?
    val activeNetworkInfo = connectivityManager!!.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}


fun View.hideSoftKeyboard(): Boolean {
    val imm = context.getSystemService(
        Context.INPUT_METHOD_SERVICE
    ) as InputMethodManager
    return imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.goneUnless(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}

fun View.hideKeyboard(): Boolean {
    try {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    } catch (ignored: RuntimeException) {
    }
    return false
}

fun View.keyboardIsShow(): Boolean {
    try {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return inputMethodManager.isActive
    } catch (ignored: RuntimeException) {
    }
    return false
}

fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}