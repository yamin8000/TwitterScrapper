@file:Suppress("unused")

package io.github.yamin8000.twitterscrapper.web

import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.errorStyle
import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.infoStyle
import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.menuStyle
import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.resultStyle
import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.t
import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.warningStyle
import io.github.yamin8000.twitterscrapper.util.Constants.instances
import okhttp3.*
import okio.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.jvm.Throws

private var client = OkHttpClient()

suspend fun retryingGet(
    partialUrl: String,
    retries: Int = 0,
    base: String = instances[retries],
    retriesLimit: Int = instances.size
): Response? = try {
    t.println(infoStyle("Retrying call for $base$partialUrl retrying: $retries/$retriesLimit"))
    val response = get("$base$partialUrl")
    if (response.isSuccessful) response
    else throw Exception("Request for $base$partialUrl failed, retrying.")
} catch (e: Exception) {
    t.println(errorStyle("Retrying call for $base$partialUrl failed."))
    t.println(errorStyle(e.message ?: ""))
    if (retries < retriesLimit) {
        retryingGet(partialUrl, retries + 1, base, retriesLimit)
    } else null
}

/**
 * This hungry implementation of http get call using OkHttp,
 * is consistent to get a successful result by costing
 * any resource available by calling itself recursively
 */
suspend fun urgentGet(
    url: String
): Response = try {
    t.println(warningStyle("Urgent call for $url"))
    val response = get(url)
    if (response.isSuccessful) response
    else urgentGet(url)
} catch (e: Exception) {
    t.println(errorStyle("Urgent call failed: ${e.message}"))
    urgentGet(url)
}

/**
 * A simple coroutine wrapper for OkHttp HTTP GET callback call
 */
@Throws(IOException::class)
suspend fun get(
    url: String
) = suspendCoroutine { continuation ->
    t.println(infoStyle("Enqueuing new call for $url"))
    client.newCall(Request.Builder().url(url).build())
        .enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                t.println(errorStyle("Call failed with: ${e.message} for $url"))
                continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                t.println(resultStyle("Call completed: HTTP ${menuStyle(response.code.toString())} for $url"))
                continuation.resume(response)
            }
        })
}