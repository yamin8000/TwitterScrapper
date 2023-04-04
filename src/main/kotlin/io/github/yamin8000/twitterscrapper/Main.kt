package io.github.yamin8000.twitterscrapper

import io.github.yamin8000.twitterscrapper.modules.MainModule
import io.github.yamin8000.twitterscrapper.modules.crawler.Crawler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

fun main() {
    MainModule().run()
}

fun fetchTweets() {
    val crawler = Crawler(isNested = false)
    runBlocking {
        withContext(Dispatchers.Default) {
            crawler.crawl()
        }
    }
}