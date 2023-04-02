package io.github.yamin8000.twitterscrapper

import io.github.yamin8000.twitterscrapper.helpers.client
import io.github.yamin8000.twitterscrapper.helpers.httpGet
import io.github.yamin8000.twitterscrapper.util.Constants
import io.github.yamin8000.twitterscrapper.util.Constants.DEFAULT_TWEETS_LIMIT
import io.github.yamin8000.twitterscrapper.util.Constants.ERROR_503
import io.github.yamin8000.twitterscrapper.util.Constants.askStyle
import io.github.yamin8000.twitterscrapper.util.Constants.errorStyle
import io.github.yamin8000.twitterscrapper.util.Constants.infoStyle
import io.github.yamin8000.twitterscrapper.util.Constants.instances
import io.github.yamin8000.twitterscrapper.util.Constants.resultStyle
import io.github.yamin8000.twitterscrapper.util.Constants.t
import io.github.yamin8000.twitterscrapper.util.Constants.warningStyle
import io.github.yamin8000.twitterscrapper.util.Utility.csvOf
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.io.File
import java.util.regex.Pattern
import kotlin.random.Random
import kotlin.random.nextInt

class Crawler {
    private val scope = CoroutineScope(Dispatchers.IO)

    private var startingUsers = listOf<String>()

    private var triggers: List<String> = listOf()

    init {
        var line = ""
        while (line.isBlank()) {
            t.println(askStyle("Enter name of the starting users like this:"))
            t.println(infoStyle("ali,reza,hamed"))
            line = readlnOrNull() ?: ""
            t.println(askStyle("Enter tweet limit for each user or just press enter to get default number."))
            val limit = readlnOrNull()
            if (!limit.isNullOrBlank()) {
                DEFAULT_TWEETS_LIMIT = limit.toInt()
            }
            t.println(askStyle("Enter triggers like this or just press enter to get all."))
            t.println(infoStyle("iran,asia"))
            val triggerLine = readlnOrNull()
            if (!triggerLine.isNullOrBlank()) {
                triggers = triggerLine.split(',')
            }
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
            t.println(infoStyle("crawling: $username"))
            val cleanUsername = username.sanitizeUser()
            if (cleanUsername.isNotBlank()) {
                val file = File("${Constants.DOWNLOAD_PATH}/${cleanUsername}.txt")
                if (!file.exists()) {
                    val (tweets, newUsers) = getTweetsWithUsers(cleanUsername)
                    if (newUsers.isNotEmpty()) {
                        t.println(
                            infoStyle(
                                "new users from $cleanUsername are ${
                                    newUsers.take(5).joinToString()
                                } and maybe more."
                            )
                        )
                    } else t.println(warningStyle("$cleanUsername has no friends"))
                    if (tweets.isNotEmpty())
                        saveUserPosts(cleanUsername, tweets)
                    else t.println(infoStyle("$cleanUsername has no tweets"))
                    buildList {
                        newUsers.map { it.substring(1) }.forEach {
                            add(scope.launch { singleUserCrawler(it) })
                        }
                    }.joinAll()
                } else t.println(warningStyle("$cleanUsername exists"))
            }
        }
    }

    private suspend fun getTweetsWithUsers(
        username: String,
        limit: Int = DEFAULT_TWEETS_LIMIT,
        base: String = "https://nitter.net/"
    ): Pair<Set<String>, Set<String>> {
        var tempBase = base
        var cursor = ""

        var tweets = mutableListOf<String>()
        var hasMoreIndicator: String

        var html: String
        var failedScrap: Boolean

        t.println(infoStyle("fetching $username posts from $tempBase"))
        do {
            do {
                html = client.httpGet("$tempBase$username?cursor=$cursor")
                failedScrap = html.isBlank() || html.contains(ERROR_503)
                if (failedScrap) {
                    t.println(errorStyle("### ==> $ERROR_503 or failed request <== ### for $username with instance: $tempBase"))
                    tempBase = instances[Random.nextInt(instances.indices)]
                    delay(Random.nextLong(50L, 500L))
                }
            } while (failedScrap)
            val doc = Jsoup.parse(html)
            hasMoreIndicator = doc.select("div[class^=show-more] a").attr("href")
            cursor = hasMoreIndicator.split('=').last()
            val threads = doc.select("div[class^=thread-line]")
            tweets.addAll(getSingles(doc.allElements))
            tweets.addAll(getSingles(threads))
            if (tweets.size >= limit)
                break
        } while (hasMoreIndicator.isNotBlank())
        val newUsers = fetchNewUsers(html).map { it.lowercase() }.toMutableList()
        newUsers.remove("@$username")
        triggers.forEach { trigger ->
            tweets = tweets.filter { it.contains(trigger) }.toMutableList()
        }
        return tweets.take(limit).map { removeLinks(it) }.toSet() to newUsers.toSet()
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
        posts: Set<String>
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
        tweet: String
    ): String {
        return try {
            Pattern.compile("<a.*</a>").matcher(tweet).replaceAll {
                it.group().substringAfter(">").substringBefore("</a>")
            }
        } catch (e: Exception) {
            tweet
        }
    }

    private fun String?.sanitizeUser() = this?.lowercase()?.trim() ?: ""
}