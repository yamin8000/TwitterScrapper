package io.github.yamin8000.twitterscrapper

import io.github.yamin8000.twitterscrapper.helpers.client
import io.github.yamin8000.twitterscrapper.helpers.httpGet
import org.jsoup.Jsoup
import org.jsoup.select.Elements

fun main() {
    val base = "https://nitter.net/"
    val profile = "yamin"
    var cursor = ""

    val twits = mutableListOf<String>()
    var hasMoreIndicator = ""

    var html: String
    do {
        html = client.httpGet("$base$profile?cursor=$cursor")
        if (html.isNotBlank()) {
            val doc = Jsoup.parse(html)
            hasMoreIndicator = doc.select("div[class^=show-more] a").attr("href")
            cursor = hasMoreIndicator.split('=').last()
            val threads = doc.select("div[class^=thread-line]")
            twits.addAll(getSingles(doc.allElements))
            twits.addAll(getSingles(threads))
            println("twits  count: ${twits.size}")
        }
    } while (hasMoreIndicator.isNotBlank())
    twits.forEachIndexed { i, item ->
        println(i)
        println(item)
    }
}

private fun getSingles(elements: Elements): List<String> {
    return elements.select("div[class^=timeline-item]").map {
        it.select("div[class^=tweet-content]").html()
    }
}