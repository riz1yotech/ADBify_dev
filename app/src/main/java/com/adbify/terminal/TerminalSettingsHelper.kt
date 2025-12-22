package com.adbify.terminal

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.adbify.utils.AndroidUtilities

object TerminalSettingsHelper {
    private const val KEY_KEEP_SCREEN_ON = "key_keep_screen_on"
    private const val KEY_FONT_SIZE = "key_font_size"

    private var DEFAULT_FONT_SIZE = 0
    private var MIN_FONT_SIZE = 0
    private var MAX_FONT_SIZE = 0

    // Terminal
    fun changeFontSize(context: Context, increase: Boolean) {
        var fontSize = getFontSize(context)
        fontSize += (if (increase) 1 else -1) * 2
        fontSize = fontSize.coerceIn(MIN_FONT_SIZE, MAX_FONT_SIZE)
        setFontSize(context, fontSize)
    }

    fun getFontSize(context: Context): Int {
        return getPreferences(context).getInt(KEY_FONT_SIZE, DEFAULT_FONT_SIZE)
    }

    fun setFontSize(context: Context, value: Int) {
        getPreferences(context).edit(commit = true) {
            putInt(KEY_FONT_SIZE, value)
        }
    }

    fun shouldKeepScreenOn(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_KEEP_SCREEN_ON, true)
    }

    fun setKeepScreenOn(context: Context, value: Boolean) {
        getPreferences(context).edit(commit = true) {
            putBoolean(KEY_KEEP_SCREEN_ON, value)
        }
    }

    fun getPreferences(context: Context): SharedPreferences {
        DEFAULT_FONT_SIZE = AndroidUtilities.dpToPx(context, 12f).toInt()
        MIN_FONT_SIZE = AndroidUtilities.dpToPx(context, 8f).toInt()
        MAX_FONT_SIZE = AndroidUtilities.dpToPx(context, 20f).toInt()
        return context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    }
}

