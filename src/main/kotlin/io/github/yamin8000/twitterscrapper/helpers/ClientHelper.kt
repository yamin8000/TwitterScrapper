package io.github.yamin8000.twitterscrapper.helpers

import okhttp3.*
import okio.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal var client = OkHttpClient()

suspend fun OkHttpClient.httpGet(url: String): Response {
    return suspendCoroutine { continuation ->
        this.newCall(Request.Builder().url(url).build())
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    continuation.resume(response)
                }
            })
    }
}