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
import io.github.yamin8000.twitterscrapper.util.Constants.t
import io.github.yamin8000.twitterscrapper.util.Constants.warningStyle
import io.github.yamin8000.twitterscrapper.util.Utility.csvOf
import kotlinx.coroutines.*
import okhttp3.Response
import okio.IOException
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.io.File
import java.util.regex.Pattern
import kotlin.random.Random
import kotlin.random.nextInt

class Crawler(
    private val isNested: Boolean = true
) {
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
        buildList {
            startingUsers.forEach { user ->
                add(scope.launch { singleUserCrawler(user) })
            }
        }.joinAll()
    }

    private suspend fun singleUserCrawler(
        username: String
    ) {
        t.println(infoStyle("crawling: $username"))
        val cleanUsername = username.sanitizeUser()
        if (cleanUsername.isNotBlank()) {
            val file = File("${Constants.DOWNLOAD_PATH}/${cleanUsername}.txt")
            if (!file.exists()) {
                val (tweets, newUsers) = withContext(scope.coroutineContext) { getTweetsWithUsers(cleanUsername) }
                if (tweets.isNotEmpty()) {
                    saveUserPosts(cleanUsername, tweets.filter { it.isNotBlank() }.toSet())
                    if (isNested) {
                        if (newUsers.isNotEmpty()) {
                            t.println(
                                infoStyle(
                                    "new users from $cleanUsername are ${
                                        newUsers.take(5).joinToString()
                                    } and maybe more."
                                )
                            )
                        } else t.println(warningStyle("$cleanUsername has no friends"))
                        buildList {
                            newUsers.map { it.substring(1) }.forEach {
                                add(scope.launch { singleUserCrawler(it) })
                            }
                        }.joinAll()
                    }
                } else t.println(warningStyle("$cleanUsername has no tweets"))
            } else t.println(warningStyle("$cleanUsername exists"))
        }
    }

    /**
     * God Method, it should be refactored to a class
     */
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
        var totalTweets: Int
        val newUsers = mutableListOf<String>()

        t.println(infoStyle("fetching $username posts from $tempBase"))
        pagingLoop@ do {
            requestLoop@ do {
                var response: Response? = null
                try {
                    response = client.httpGet("$tempBase$username?cursor=$cursor")
                    html = response.body.string()
                } catch (e: Exception) {
                    html = ""
                    t.println(errorStyle((e as IOException).message ?: "http get error"))
                }
                when (response?.code) {
                    404 -> {
                        t.println(warningStyle("$username not found"))
                        break@pagingLoop
                    }

                    in 400..499 -> {
                        break@pagingLoop
                    }

                    503 -> {
                        t.println(errorStyle("### ==> $ERROR_503 or failed request <== ### for $username with instance: $tempBase"))
                        tempBase = instances[Random.nextInt(instances.indices)]
                        delay(Random.nextLong(50L, 500L))
                    }

                    in 500..599 -> {
                        continue
                    }

                    else -> break@requestLoop
                }
            } while (true)
            val doc = Jsoup.parse(html)
            totalTweets = doc.getElementById("profile-stat-num")?.text()?.toInt() ?: 0
            hasMoreIndicator = doc.select("div[class^=show-more] a").attr("href")
            val threads = doc.select("div[class^=thread-line]")
            tweets.addAll(getSingles(doc.allElements))
            tweets.addAll(getSingles(threads))
            cursor = hasMoreIndicator.split('=').last()
            newUsers.addAll(fetchNewUsers(html).map { it.lowercase() }.toMutableList())
            if (tweets.size >= limit)
                break
            if (tweets.isEmpty() && totalTweets != 0)
                continue
        } while (hasMoreIndicator.isNotBlank())
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
        tweets: Set<String>
    ) {
        t.println(infoStyle("saving $username tweets"))
        val file = File("${Constants.DOWNLOAD_PATH}/$username.txt")

        if (file.length() == 0L) {
            val csv = csvOf(
                headers = listOf("#", "tweet"),
                data = tweets,
                itemBuilder = { index, item ->
                    listOf("$index", item)
                }
            )
            csv.split("\n").forEach { line ->
                t.println(infoStyle("saving $username tweet: ${line.take(40)}"))
                file.appendText(line)
                file.appendText("\n")
            }
        } else t.println(warningStyle("$username is already saved"))
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
            }.trim()
        } catch (e: Exception) {
            tweet
        }
    }

    private fun String?.sanitizeUser() = this?.lowercase()?.trim() ?: ""
}