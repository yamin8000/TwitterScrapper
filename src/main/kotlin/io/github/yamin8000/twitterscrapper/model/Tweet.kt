package io.github.yamin8000.twitterscrapper.model

data class Tweet(
    val content: String,
    val date: String,
    val link: String,
    val contentType: TweetContentType,
    val user: User,
    val stats: TweetStats,
    val isRetweet: Boolean,
    val isThreaded: Boolean,
    val isPinned: Boolean = false,
    val replies: List<Tweet> = listOf(),
    val originalTweeter: User? = null,
    val quote: Tweet? = null,
    val thread: String? = null,
)
