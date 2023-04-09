package io.github.yamin8000.twitterscrapper.modules.crawler

import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.errorStyle
import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.infoStyle
import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.readBoolean
import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.readInteger
import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.readMultipleStrings
import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.resultStyle
import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.t
import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.warningStyle
import io.github.yamin8000.twitterscrapper.util.Constants
import io.github.yamin8000.twitterscrapper.util.Constants.DEFAULT_CRAWL_TWEETS_LIMIT
import io.github.yamin8000.twitterscrapper.util.Constants.PROTECTED_ACCOUNT
import io.github.yamin8000.twitterscrapper.util.Utility.csvOf
import io.github.yamin8000.twitterscrapper.web.retryingGet
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.io.File

class Crawler(
    private val isNested: Boolean = true,
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private var startingUsers = listOf<String>()

    private var tweetCountLimit = DEFAULT_CRAWL_TWEETS_LIMIT

    private var triggers: List<String> = listOf()

    init {
        startingUsers = readMultipleStrings("Starting user").map { it.sanitizeUser() }
        if (readBoolean("Do you want to limit the number of tweets for each user?(y/n)")) {
            tweetCountLimit = readInteger(
                message = "Enter tweet limit for each user.",
                range = 1..DEFAULT_CRAWL_TWEETS_LIMIT
            )
        }
        if (readBoolean("Do you want to filter tweets with Trigger words?(y/n)"))
            triggers = readMultipleStrings("Trigger word")
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
        t.println(infoStyle("Crawling: ") + resultStyle(username))

        if (!File("${Constants.DOWNLOAD_PATH}/$username.txt").exists()) {
            var tweetCount = 0
            try {
                crawlUsername(username) { elements ->
                    t.println(infoStyle("New results for $username"))
                    val tweets = getSingles(elements)
                    val tweetsWithTriggers = mutableListOf<String>()
                    triggers.forEach { trigger ->
                        tweetsWithTriggers.addAll(tweets.filter { it.contains(trigger) })
                    }

                    var newTweets = tweets
                    var newTweetsCount = newTweets.size
                    if (triggers.isNotEmpty()) {
                        newTweets = tweetsWithTriggers
                        newTweetsCount = tweetsWithTriggers.size
                    }

                    if (newTweets.isNotEmpty())
                        saveUserPosts(username, newTweets.take(tweetCountLimit).toSet())
                    else t.println(warningStyle("Empty tweets for $username"))

                    tweetCount += newTweetsCount
                    if (tweetCount >= tweetCountLimit) throw Exception("Tweet count limit reached for $username")

                    val friends = fetchNewUsers(elements.html())
                        .map { it.sanitizeUser() }
                        .filter { it != username }
                    if (isNested)
                        friends.forEach { scope.launch { singleUserCrawler(it) } }
                }
            } catch (e: Exception) {
                t.println(errorStyle(e.message ?: ""))
            }
        } else t.println(warningStyle("$username is already being crawled"))
    }

    private suspend fun crawlUsername(
        username: String,
        onNewElements: (Elements) -> Unit
    ) {
        var cursor: String? = ""
        var html: String
        do {
            html = withContext(scope.coroutineContext) { retryingGet("$username?cursor=$cursor")?.body?.string() ?: "" }
            if (html.contains(PROTECTED_ACCOUNT)) {
                t.println(errorStyle(PROTECTED_ACCOUNT))
                break
            }
            val doc = Jsoup.parse(html)
            val error = doc.selectFirst("div[class^=error-panel]")
            if (error != null) {
                t.println(errorStyle(error.selectFirst("span")?.text() ?: ""))
                break
            }
            cursor = doc.selectFirst("div[class^=show-more] a")
                ?.attr("href")
                ?.split('=')
                ?.last()
            onNewElements(doc.allElements)
        } while (cursor != null)
    }

    private fun getSingles(
        elements: Elements
    ) = elements.select("div[class^=timeline-item]").map {
        it.selectFirst("div[class^=tweet-content]")?.text() ?: ""
    }.filter { it.isNotBlank() }

    private fun saveUserPosts(
        username: String,
        tweets: Set<String>
    ) {
        t.println(infoStyle("Saving $username tweets"))
        val file = File("${Constants.DOWNLOAD_PATH}/$username.txt")

        var bias = 0
        var headers: List<String>? = listOf("#", "tweet")
        if (file.exists()) {
            bias = file.readText().split("\n").size
            headers = null
        }

        val csv = csvOf(
            indexBias = bias,
            headers = headers,
            data = tweets,
            itemBuilder = { index, item ->
                listOf("$index", item)
            }
        )

        csv.split("\n").forEach { line ->
            file.appendText(line)
            file.appendText("\n")
        }
    }

    private fun fetchNewUsers(
        html: String
    ) = Constants.twitterProfileRegex.findAll(html).map { it.value.lowercase() }.toSet()

    private fun String?.sanitizeUser() = this?.lowercase()?.trim()?.removePrefix("@") ?: ""
}