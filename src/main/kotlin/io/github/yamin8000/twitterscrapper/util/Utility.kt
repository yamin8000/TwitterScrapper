package io.github.yamin8000.twitterscrapper.util

import kotlin.reflect.full.memberProperties

object Utility {
    fun <T> csvOf(
        data: Iterable<T>,
        headers: Iterable<String>? = null,
        indexBias: Int = 0,
        itemBuilder: (Int, T) -> Iterable<String>
    ) = buildString {
        if (headers != null)
            append(headers.joinToString(",") { "\"$it\"" })
        append("\n")
        data.forEachIndexed { index, item ->
            append(itemBuilder(index + indexBias, item).joinToString(",") { "\"$it\"" })
            append("\n")
        }
    }.trim()

    fun <T> Iterable<T>.csv(
        headers: Iterable<String> = this.first()!!::class.memberProperties.map { it.name },
        itemBuilder: (Int, T) -> Iterable<String> = { _, t ->
            this.first()!!::class.memberProperties.map { "${it.getter.call(t)}" }
        }
    ) = csvOf(headers = headers, data = this, itemBuilder = itemBuilder)

    fun String.sanitizeUsername() = this.lowercase().trim().removePrefix("@")

    fun String?.sanitizeNum() = this?.filter { it != ',' }?.toIntOrNull() ?: 0
}