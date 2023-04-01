package io.github.yamin8000.twitterscrapper

import io.github.yamin8000.twitterscrapper.helpers.client
import io.github.yamin8000.twitterscrapper.helpers.httpGet
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.util.regex.Pattern

fun main() {
    println("Enter username to fetch posts:")
    val username = readlnOrNull()
    if (username != null) {
        getUserPosts(username).forEachIndexed { index, item ->
            println(index)
            println(item)
        }
    }
}

private fun getUserPosts(
    username: String,
    limit: Int = 100
): List<String> {
    val base = "https://nitter.net/"
    var cursor = ""

    val twits = mutableListOf<String>()
    var hasMoreIndicator = ""

    var html: String
    do {
        html = client.httpGet("$base$username?cursor=$cursor")
        if (html.isNotBlank()) {
            val doc = Jsoup.parse(html)
            hasMoreIndicator = doc.select("div[class^=show-more] a").attr("href")
            cursor = hasMoreIndicator.split('=').last()
            val threads = doc.select("div[class^=thread-line]")
            twits.addAll(getSingles(doc.allElements))
            twits.addAll(getSingles(threads))
            if (twits.size >= limit)
                break
        }
    } while (hasMoreIndicator.isNotBlank())
    return twits.take(limit).map { removeLinks(it) }
}

fun removeLinks(
    twit: String
): String {
    return Pattern.compile("<a.*</a>").matcher(twit).replaceAll {
        it.group().substringAfter(">").substringBefore("</a>")
    }
}

private fun getSingles(elements: Elements): List<String> {
    return elements.select("div[class^=timeline-item]").map {
        it.select("div[class^=tweet-content]").html()
    }
}