package io.github.yamin8000.twitterscrapper.util

import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.t
import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.table
import kotlin.reflect.full.memberProperties

object Utility {
    fun <T> csvOf(
        headers: Iterable<String>,
        data: Iterable<T>,
        itemBuilder: (Int, T) -> Iterable<String>
    ) = buildString {
        append(headers.joinToString(",") { "\"$it\"" })
        append("\n")
        data.forEachIndexed { index, item ->
            append(itemBuilder(index, item).joinToString(",") { "\"$it\"" })
            append("\n")
        }
    }

    fun <T> Iterable<T>.csv(
        headers: Iterable<String> = this.first()!!::class.memberProperties.map { it.name },
        itemBuilder: (Int, T) -> Iterable<String> = { _, t ->
            this.first()!!::class.memberProperties.map { "${it.getter.call(t)}" }
        }
    ) = csvOf(headers, this, itemBuilder)

    fun String.sanitizeUsername() = this.lowercase().trim().removePrefix("@")
}