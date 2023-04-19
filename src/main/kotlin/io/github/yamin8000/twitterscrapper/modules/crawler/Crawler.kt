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
import io.github.yamin8000.twitterscrapper.util.Constants.DEFAULT_CRAWL_DEPTH_LIMIT
import io.github.yamin8000.twitterscrapper.util.Constants.DEFAULT_CRAWL_TWEETS_LIMIT
import io.github.yamin8000.twitterscrapper.util.Constants.PROTECTED_ACCOUNT
import io.github.yamin8000.twitterscrapper.util.KTree
import io.github.yamin8000.twitterscrapper.util.Utility.csvOf
import io.github.yamin8000.twitterscrapper.web.retryingGet
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.io.File

class Crawler(
    isNested: Boolean = true
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val channel = Channel<Job>(4)

    private val startingUsers: List<String>

    private var tweetCountLimit = DEFAULT_CRAWL_TWEETS_LIMIT

    private var triggers = listOf<String>()

    private var root: KTree<String>? = null

    private var depthLimit = DEFAULT_CRAWL_DEPTH_LIMIT

    init {
        startingUsers = readMultipleStrings("Starting user").map { it.sanitizeUser() }
        if (readBoolean("Do you want to customize the crawler?")) {
            if (readBoolean("Do you want to limit the number of tweets for each user?")) {
                tweetCountLimit = readInteger(
                    message = "Enter tweet limit for each user.",
                    range = 1..DEFAULT_CRAWL_TWEETS_LIMIT
                )
            }
            if (isNested) {
                if (readBoolean("Do you want to specify crawl depth limit?"))
                    depthLimit = readInteger("Crawl depth limit")
            } else depthLimit = 1
            if (readBoolean("Do you want to filter tweets with Trigger words?"))
                triggers = readMultipleStrings("Trigger word")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun crawl() {
        startingUsers.forEach { user ->
            channel.send(singleUserCrawler(user))
        }
        while (true) {
            if (channel.isEmpty) {
                t.println(resultStyle("Crawler Stopped!"))
                break
            }
            delay(5000)
        }
    }

    private suspend fun singleUserCrawler(
        username: String
    ): Job = scope.launch {
        if (root == null) root = KTree(username)

        t.println(infoStyle("Crawling: ") + resultStyle(username))

        if (!File("${Constants.DOWNLOAD_PATH}/$username.txt").exists()) {
            val (tweets, friends) = crawlUsername(username, tweetCountLimit)
            t.println(infoStyle("New results for $username"))
            val tweetsWithTriggers = mutableListOf<String>()
            triggers.forEach { trigger ->
                tweetsWithTriggers.addAll(tweets.filter { it.contains(trigger) })
            }

            val newTweets = if (triggers.isNotEmpty()) tweetsWithTriggers else tweets

            val node = root?.findDescendant(username) ?: root
            t.println(infoStyle("$username, Tree level: ${node?.level}"))
            if (tweets.isNotEmpty() && node != null && node.level <= depthLimit) {
                if (newTweets.isNotEmpty())
                    saveUserPosts(username, newTweets.take(tweetCountLimit).toSet())

                friends.forEach {
                    node.addChild(it)
                }
                if (depthLimit >= 1) {
                    node.children().filter { it.level <= depthLimit }.forEach {
                        channel.send(singleUserCrawler(it.data))
                    }
                }
            } else t.println(warningStyle("Empty tweets for $username"))
        } else t.println(warningStyle("$username is already being crawled"))
        channel.receive()
    }

    private suspend fun crawlUsername(
        username: String,
        limit: Int
    ): Pair<List<String>, List<String>> {
        var cursor: String? = ""
        var html: String
        val tweets = mutableSetOf<String>()
        val friends = mutableSetOf<String>()
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
            tweets.addAll(getSingles(doc.allElements))
            friends.addAll(
                fetchNewUsers(html)
                    .map { it.sanitizeUser() }
                    .filter { it != username }
            )
            if (tweets.take(limit).size >= limit) break
        } while (cursor != null)
        return tweets.take(limit) to friends.toList()
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

        val headers = listOf("#", "tweet")

        val csv = csvOf(
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