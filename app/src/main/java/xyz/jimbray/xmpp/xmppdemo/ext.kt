package xyz.jimbray.xmpp.xmppdemo

import android.content.Context
import android.provider.Settings
import android.util.Log
import android.widget.Toast

/**
 * Created by Jimbray on 2017/12/29.
 */
fun Context.toast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun log_d(tag: String, msg: String) {
    if (BuildConfig.DEBUG) {
        Log.d(tag, msg)
    }
}

fun log_v(tag: String, msg: String) {
    if (BuildConfig.DEBUG) {
        Log.v(tag, msg)
    }
}

fun log_e(tag: String, msg: String) {
    if (BuildConfig.DEBUG) {
        Log.e(tag, msg)
    }
}

fun log_w(tag: String, msg: String) {
    if (BuildConfig.DEBUG) {
        Log.w(tag, msg)
    }
}

fun log_i(tag: String, msg: String) {
    if (BuildConfig.DEBUG) {
        Log.i(tag, msg)
    }
}

fun Context.getAndroidID(): String {
    return Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID)
}