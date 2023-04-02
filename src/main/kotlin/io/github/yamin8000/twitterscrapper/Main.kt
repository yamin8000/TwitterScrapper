package io.github.yamin8000.twitterscrapper

import io.github.yamin8000.twitterscrapper.util.Constants
import io.github.yamin8000.twitterscrapper.util.FileUtils
import kotlinx.coroutines.*

fun main() {
    init()
    val crawler = Crawler()
    runBlocking {
        withContext(Dispatchers.Default) {
            crawler.crawl()
        }
    }
}

private fun init() {
    FileUtils.createDirIfNotExists(Constants.DOWNLOAD_PATH)
}