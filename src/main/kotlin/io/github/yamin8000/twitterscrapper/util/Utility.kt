package io.github.yamin8000.twitterscrapper.util

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
}