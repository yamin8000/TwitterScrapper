package io.github.yamin8000.twitterscrapper

import io.github.yamin8000.twitterscrapper.helpers.client
import io.github.yamin8000.twitterscrapper.helpers.httpGet
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.io.File
import java.nio.file.Files
import java.util.regex.Pattern
import kotlin.io.path.Path

private val userRegex = Regex("@[a-zA-Z0-9_]+")
private const val LIMIT = 3

fun main() {
    crawl()
}

private fun crawl(
    username: String? = null
) {
    if (username != null) {
        val file = File("data/${username.lowercase()}.txt")
        if (!file.exists()) {
            val (posts, newUsers) = getUsers(username.lowercase(), 100)
            saveUserPosts(username.lowercase(), posts)
            newUsers.forEach { crawl(it.substring(1)) }
        } else println("$username exists")
    } else {
        println("Enter username to fetch posts:")
        crawl(readlnOrNull())
    }
}

private fun saveUserPosts(
    username: String,
    posts: List<String>
) {
    println("saving $username")
    val file = File("data/$username.txt")
    posts.forEachIndexed { index, line ->
        println("tweet #$index for $username")
        file.appendText("$line\n")
    }
}

private fun getUsers(
    username: String,
    limit: Int = 100
): Pair<List<String>, Set<String>> {
    val base = "https://nitter.net/"
    var cursor = ""

    val tweets = mutableListOf<String>()
    var hasMoreIndicator = ""

    var html: String
    do {
        html = client.httpGet("$base$username?cursor=$cursor")
        if (html.isNotBlank()) {
            val doc = Jsoup.parse(html)
            hasMoreIndicator = doc.select("div[class^=show-more] a").attr("href")
            cursor = hasMoreIndicator.split('=').last()
            val threads = doc.select("div[class^=thread-line]")
            tweets.addAll(getSingles(doc.allElements))
            tweets.addAll(getSingles(threads))
            if (tweets.size >= limit)
                break
        }
    } while (hasMoreIndicator.isNotBlank())
    val newUsers = fetchNewUsers(html).map { it.lowercase() }.toMutableList()
    newUsers.remove("@$username")
    return tweets.take(limit).map { removeLinks(it) } to newUsers.toSet()
}

fun fetchNewUsers(html: String) = userRegex.findAll(html).map { it.value.lowercase() }.toSet()

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