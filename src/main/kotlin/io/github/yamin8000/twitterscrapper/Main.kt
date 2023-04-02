package io.github.yamin8000.twitterscrapper

import com.github.ajalt.mordant.rendering.BorderType
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.table.table
import io.github.yamin8000.twitterscrapper.util.Constants.DOWNLOAD_FOLDER
import io.github.yamin8000.twitterscrapper.util.Constants.DOWNLOAD_PATH
import io.github.yamin8000.twitterscrapper.util.Constants.askStyle
import io.github.yamin8000.twitterscrapper.util.Constants.infoStyle
import io.github.yamin8000.twitterscrapper.util.Constants.mainMenu
import io.github.yamin8000.twitterscrapper.util.Constants.menuStyle
import io.github.yamin8000.twitterscrapper.util.Constants.t
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

private val config = Config()
fun main() {
    val lines = mainMenu.split("\n")
    t.println(table {
        borderType = BorderType.ROUNDED
        borderStyle = TextColors.brightBlue
        body { lines.forEach { row(menuStyle(it)) } }
    })

    val menu = readlnOrNull()
    if (menu != null) {
        when (menu) {
            "1" -> crawler()
            "2" -> fetchTweets()
            "3" -> settings()
        }
    }
}

private fun crawler() {
    val crawler = Crawler()
    runBlocking {
        withContext(Dispatchers.Default) {
            crawler.crawl()
        }
    }
}

fun fetchTweets() {
    val crawler = Crawler(isNested = false)
    runBlocking {
        withContext(Dispatchers.Default) {
            crawler.crawl()
        }
    }
}

fun settings() {
    t.println(infoStyle("current download folder is: $DOWNLOAD_PATH"))
    val prompt = readlnOrNull() ?: "n"
    if (prompt == "y") {
        t.println(askStyle("enter new download path"))
        DOWNLOAD_PATH = readlnOrNull() ?: DOWNLOAD_PATH
        config.updateConfigFile(DOWNLOAD_FOLDER to DOWNLOAD_PATH)
    }
}
