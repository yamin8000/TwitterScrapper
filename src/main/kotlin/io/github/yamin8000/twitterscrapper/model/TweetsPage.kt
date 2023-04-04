package io.github.yamin8000.twitterscrapper.model

data class TweetsPage(
    val tweets: List<Tweet>,
    val cursor: String?
)
