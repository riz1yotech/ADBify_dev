package com.adbify.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import java.io.Closeable
import java.io.IOException
import java.lang.ref.WeakReference
import java.nio.charset.StandardCharsets

object AndroidUtilities {
    private var sToast: WeakReference<Toast>? = null

    fun closeQuietly(c: Closeable?) {
        c?.let {
            try {
                it.close()
            } catch (ignored: IOException) {
                // Ignored
            }
        }
    }

    fun isServiceRunning(context: Context, clazz: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (clazz.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun killServiceIfRunning(context: Context, clazz: Class<*>) {
        val intent = Intent(context, clazz)
        if (isServiceRunning(context, clazz)) {
            context.stopService(intent)
        }
    }

    fun getPid(p: Process): Int {
        return try {
            val f = p.javaClass.getDeclaredField("pid")
            f.isAccessible = true
            try {
                f.getInt(p)
            } finally {
                f.isAccessible = false
            }
        } catch (e: Throwable) {
            -1
        }
    }

    fun dpToPx(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        )
    }

    fun pxToDp(context: Context, px: Float): Float {
        return px / context.resources.displayMetrics.density
    }

    fun setLayoutMarginsInDp(view: View, left: Int, top: Int, right: Int, bottom: Int) {
        val context = view.context
        setLayoutMarginsInPixels(
            view,
            dpToPx(context, left.toFloat()).toInt(),
            dpToPx(context, top.toFloat()).toInt(),
            dpToPx(context, right.toFloat()).toInt(),
            dpToPx(context, bottom.toFloat()).toInt()
        )
    }

    fun setLayoutMarginsInPixels(view: View, left: Int, top: Int, right: Int, bottom: Int) {
        if (view.layoutParams is ViewGroup.MarginLayoutParams) {
            val params = view.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(left, top, right, bottom)
            view.layoutParams = params
        }
    }

    fun getDrawable(context: Context, @DrawableRes drawable: Int): Drawable? {
        return ResourcesCompat.getDrawable(
            context.resources,
            drawable,
            context.theme
        )
    }

    fun showSoftKeyboard(view: View?) {
        view ?: return
        try {
            val inputManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hideSoftKeyboard(view: View?) {
        view ?: return
        try {
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (!imm.isActive) {
                return
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toast(context: Context, msg: CharSequence?) {
        toast(context, msg, Toast.LENGTH_SHORT)
    }

    fun toastLong(context: Context, msg: CharSequence?) {
        toast(context, msg, Toast.LENGTH_LONG)
    }

    fun toast(context: Context, msg: CharSequence?, duration: Int) {
        if (msg == null || duration == -1) {
            return
        }
        sToast?.get()?.cancel()
        val mToast = Toast.makeText(context, msg, duration)
        mToast.show()
        sToast = WeakReference(mToast)
    }

    fun getStringBytes(src: String): ByteArray {
        return try {
            src.toByteArray(StandardCharsets.UTF_8)
        } catch (ignore: Exception) {
            ByteArray(0)
        }
    }

    @SuppressLint("PrivateApi")
    fun getSystemProperty(key: String): String? {
        return try {
            val props = Class.forName("android.os.SystemProperties")
            props.getMethod("get", String::class.java).invoke(null, key) as String
        } catch (ignore: Exception) {
            null
        }
    }
}

