package com.adbify.utils

import android.content.Context
import android.util.Log
import com.adbify.AdbifyApp
import com.adbify.User
import org.json.JSONObject

object MoshiUtil {

    fun test(): List<User> {
        val json: String = loadJsonFromAssets(AdbifyApp.instance, "UserJson.json")
        val users = User.fromJsonList(json)
        Log.d("MoshiUtil", "Users loaded: ${users.map { it.toJson().prettyPrint() }}")
        return users
    }

    private fun loadJsonFromAssets(context: Context, fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }

    fun String.prettyPrint(): String {
        return try {
            val jsonObject = JSONObject(this)
            jsonObject.toString(2) // 4 spaces indentation
        } catch (e: Exception) {
            this // Return original string if parsing fails
        }
    }

}