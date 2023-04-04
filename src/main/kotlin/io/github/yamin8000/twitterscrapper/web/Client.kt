package io.github.yamin8000.twitterscrapper.web

import okhttp3.*
import okio.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.jvm.Throws

private var client = OkHttpClient()

/**
 * This hungry implementation of http get call using OkHttp,
 * is consistent to get a successful result by costing
 * any resource available by calling itself recursively
 */
suspend fun urgentGet(
    url: String
): Response = try {
    val response = get(url)
    if (response.isSuccessful) response
    else urgentGet(url)
} catch (e: Exception) {
    urgentGet(url)
}

/**
 * A simple coroutine wrapper for OkHttp HTTP GET callback call
 */
@Throws(IOException::class)
suspend fun get(
    url: String
) = suspendCoroutine { continuation ->
    client.newCall(Request.Builder().url(url).build())
        .enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                continuation.resume(response)
            }
        })
}