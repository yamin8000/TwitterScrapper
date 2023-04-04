package io.github.yamin8000.twitterscrapper.model

data class TweetStats(
    var replies: Int = 0,
    var retweets: Int = 0,
    var quotes: Int = 0,
    var likes: Int = 0,
)
