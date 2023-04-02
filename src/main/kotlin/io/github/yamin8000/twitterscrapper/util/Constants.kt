package io.github.yamin8000.twitterscrapper.util

object Constants {
    private const val TWITTER_PROFILE_REGEX_RULE = "@[a-zA-Z0-9_]+"
    val twitterProfileRegex = Regex(TWITTER_PROFILE_REGEX_RULE)
    const val DOWNLOAD_PATH = "d:\\TwitterScrapper"
    const val DEFAULT_TWEETS_LIMIT = 200
}