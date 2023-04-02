package io.github.yamin8000.twitterscrapper

import io.github.yamin8000.twitterscrapper.helpers.client
import io.github.yamin8000.twitterscrapper.helpers.httpGet
import io.github.yamin8000.twitterscrapper.util.Constants
import io.github.yamin8000.twitterscrapper.util.Constants.DEFAULT_TWEETS_LIMIT
import io.github.yamin8000.twitterscrapper.util.Utility.csvOf
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.io.File
import java.util.regex.Pattern

class Crawler {
    private val scope = CoroutineScope(Dispatchers.IO)

    private var startingUsers = listOf<String>()

    init {
        var line = ""
        while (line.isBlank()) {
            println("Enter name of the starting users like this:")
            println("ali,reza,hamed")
            line = readlnOrNull() ?: ""
        }
        startingUsers = line.trim().split(',').map { it.sanitizeUser() }
    }

    suspend fun crawl() {
        withContext(scope.coroutineContext) {
            buildList {
                startingUsers.forEach { user ->
                    add(scope.launch { singleUserCrawler(user) })
                }
            }.joinAll()
        }
    }

    private suspend fun singleUserCrawler(
        username: String
    ) {
        withContext(scope.coroutineContext) {
            println("crawling: $username")
            val cleanUsername = username.sanitizeUser()
            if (cleanUsername.isNotBlank()) {
                val file = File("${Constants.DOWNLOAD_PATH}/${cleanUsername}.txt")
                if (!file.exists()) {
                    val (posts, newUsers) = getUsers(cleanUsername)
                    if (newUsers.isNotEmpty())
                        println("new users from $username are ${newUsers.take(5).joinToString()} and maybe more.")
                    else println("$cleanUsername has no friends")
                    if (posts.isNotEmpty())
                        saveUserPosts(cleanUsername, posts)
                    else println("$cleanUsername has no tweets")
                    buildList {
                        newUsers.forEach {
                            add(scope.launch { singleUserCrawler(it.substring(1)) })
                        }
                    }.joinAll()
                } else println("$cleanUsername exists")
            }
        }
    }

    private fun getUsers(
        username: String,
        limit: Int = DEFAULT_TWEETS_LIMIT
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

    private fun getSingles(
        elements: Elements
    ): List<String> {
        return elements.select("div[class^=timeline-item]").map {
            it.select("div[class^=tweet-content]").html()
        }
    }

    private fun saveUserPosts(
        username: String,
        posts: List<String>
    ) {
        val file = File("${Constants.DOWNLOAD_PATH}/$username.txt")

        file.appendText(
            csvOf(
                headers = listOf("#", "tweet"),
                data = posts,
                itemBuilder = { index, item ->
                    listOf("$index", item)
                }
            )
        )
    }

    private fun fetchNewUsers(
        html: String
    ): Set<String> {
        return Constants.twitterProfileRegex.findAll(html).map { it.value.lowercase() }.toSet()
    }

    private fun removeLinks(
        twit: String
    ): String {
        return Pattern.compile("<a.*</a>").matcher(twit).replaceAll {
            it.group().substringAfter(">").substringBefore("</a>")
        }
    }

    private fun String?.sanitizeUser() = this?.lowercase()?.trim() ?: ""
}