package io.github.yamin8000.twitterscrapper.helpers

import okhttp3.*

internal var client = OkHttpClient()

fun OkHttpClient.httpGet(url: String): String {
    return try {
        this.newCall(Request.Builder().url(url).build()).execute().body?.string() ?: ""
    } catch (e: Exception) {
        ""
    }
}