package com.adbify

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi.Builder
import com.squareup.moshi.Types

@JsonClass(generateAdapter = true)
data class User(
    val id: String,
    val name: String,
    val email: String,
) {

    fun toJson(): String {
        val adapter = Builder().build().adapter(User::class.java)
        return adapter.toJson(this)
    }

    companion object {
        fun fromJson(json: String): User = Builder().build().adapter(User::class.java).fromJson(json)!!
        fun fromJsonList(json: String): List<User> {
            val type = Types.newParameterizedType(List::class.java, User::class.java)
            val adapter = Builder().build().adapter<List<User>>(type)
            return adapter.fromJson(json) ?: emptyList()
        }
    }
}
