package io.github.yamin8000.twitterscrapper.helpers

import io.github.yamin8000.twitterscrapper.helpers.UserInfoHelper.getUser
import io.github.yamin8000.twitterscrapper.model.Tweet
import io.github.yamin8000.twitterscrapper.model.TweetStats
import io.github.yamin8000.twitterscrapper.model.TweetsPage
import io.github.yamin8000.twitterscrapper.util.Constants.DEFAULT_TWEETS_LIMIT
import io.github.yamin8000.twitterscrapper.util.Constants.FAILED_REQUEST_DELAY
import io.github.yamin8000.twitterscrapper.util.Constants.instances
import io.github.yamin8000.twitterscrapper.util.Utility.sanitizeNum
import io.github.yamin8000.twitterscrapper.util.Utility.sanitizeUsername
import io.github.yamin8000.twitterscrapper.web.retryingGet
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import kotlin.random.Random
import kotlin.random.nextLong

class UserTweetsRequest(
    private val username: String,
    private val limit: Int = DEFAULT_TWEETS_LIMIT
) {
    suspend fun get(): List<Tweet> {
        var page: TweetsPage? = null
        val tweets = mutableListOf<Tweet>()
        do {
            page = getUserTweetsPage(page?.cursor ?: "")
            tweets.addAll(page.tweets)
        } while (page?.cursor != null && page.cursor?.isNotBlank() == true && tweets.size < limit)
        return tweets.take(limit)
    }

    private suspend fun getUserTweetsPage(
        cursor: String? = "",
        delayTime: Long = 0
    ): TweetsPage {
        delay(delayTime)
        val response = withContext(Dispatchers.IO) {
            retryingGet("${username.sanitizeUsername()}?cursor=$cursor")
        }
        return if (response != null) {
            if (response.isSuccessful) {
                parseUserTweetsPage(response.body.string())
            } else handleUserTweetsPageError(cursor, response.code)
        } else throw Exception("Failed to retrieve tweets page for $username")
    }

    private suspend fun handleUserTweetsPageError(
        cursor: String? = "",
        httpCode: Int
    ): TweetsPage {
        return when (httpCode) {
            404 -> throw NullPointerException("$username not found")
            503 -> getUserTweetsPage(cursor, Random.nextLong(FAILED_REQUEST_DELAY))
            else -> throw Exception("Failed to retrieve tweets page for $username")
        }
    }

    private suspend fun parseUserTweetsPage(
        html: String
    ): TweetsPage {
        val doc = Jsoup.parse(html)
        val cursor = parseCursor(doc)
        val tweets = withContext(Dispatchers.IO) {
            handleUserTweetsParsing(doc.selectFirst("div[class^=timeline-container] > div[class^=timeline]"))
        }
        return TweetsPage(tweets, cursor)
    }

    private fun parseCursor(doc: Document) = doc.selectFirst("div[class^=show-more] a")?.attr("href")

    private suspend fun handleUserTweetsParsing(timeline: Element?): List<Tweet> {
        return if (timeline != null) parseUserTweets(timeline) else listOf()
    }

    private suspend fun parseUserTweets(
        timeline: Element
    ): List<Tweet> {
        return buildList {
            timeline.children().forEach { tweet ->
                val htmlClass = tweet.className()
                if (htmlClass.startsWith("timeline-item"))
                    getTimelineItem(tweet)?.let { add(it) }
                if (htmlClass.startsWith("thread-line"))
                    tweet.children().forEach { item -> getTimelineItem(item)?.let { add(it) } }
            }
        }
    }

    private suspend fun getTimelineItem(
        tweet: Element?
    ): Tweet? {
        if (tweet != null) {
            val thread = tweet.selectFirst("a[class^=show-thread]")
            val retweet = tweet.selectFirst("div[class^=retweet-header]")
            val username = getTweetUsername(tweet)
            return Tweet(
                content = tweet.selectFirst("div[class^=tweet-content]")?.text() ?: "",
                date = getTweetDate(tweet),
                link = "${instances.first().dropLast(1)}${getTweetLink(tweet)}",
                user = if (username == null) null else getUser(username),
                stats = getTweetStats(tweet),
                isRetweet = retweet != null,
                isThreaded = thread != null,
                isPinned = tweet.selectFirst("div[class^=pinned]") != null,
                replies = listOf(),
                originalTweeter = getOriginalTweeter(retweet, username),
                quote = getQuotedTweet(tweet.selectFirst("div[class^=quote]")),
                thread = "${instances.first().dropLast(1)}${thread?.attr("href") ?: ""}"
            )
        } else return null
    }

    private suspend fun getQuotedTweet(
        quote: Element?
    ): Tweet? {
        return if (quote != null) {
            val username = getTweetUsername(quote)
            Tweet(
                content = quote.selectFirst("div[class^=quote-text]")?.text() ?: "",
                date = getTweetDate(quote),
                link = quote.selectFirst("a[class^=quote-link]")?.attr("href") ?: "",
                user = if (username == null) null else getUser(username),
                stats = TweetStats(),
                isRetweet = false,
                isThreaded = false,
                isPinned = false,
            )
        } else null
    }

    private suspend fun getOriginalTweeter(
        retweet: Element?,
        username: String?
    ) = if (retweet == null || username == null) null else getUser(username)

    private fun getTweetUsername(tweet: Element) = tweet.selectFirst("a[class^=username]")?.text()?.sanitizeUsername()

    private fun getTweetDate(tweet: Element) = tweet.selectFirst("span[class^=tweet-date] a")?.attr("title") ?: ""

    private fun getTweetLink(tweet: Element) = tweet.selectFirst("a[class^=tweet-link]")?.attr("href") ?: ""

    private fun getTweetStats(
        tweet: Element
    ): TweetStats {
        val rawStats = tweet.selectFirst("div[class^=tweet-stat]")
        val stats = TweetStats()
        rawStats?.children()?.forEach { stat ->
            val icon = stat.selectFirst("div[class^=icon-container]")
            val value = icon?.text().sanitizeNum()
            when (icon?.children()?.firstOrNull()?.className() ?: "") {
                "icon-comment" -> stats.replies = value
                "icon-retweet" -> stats.retweets = value
                "icon-quote" -> stats.quotes = value
                "icon-heart" -> stats.likes = value
            }
        }
        return stats
    }
}