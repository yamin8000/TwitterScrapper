package io.github.yamin8000.twitterscrapper

import com.github.ajalt.mordant.rendering.BorderType
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.table.table
import io.github.yamin8000.twitterscrapper.util.Constants
import io.github.yamin8000.twitterscrapper.util.Constants.infoStyle
import io.github.yamin8000.twitterscrapper.util.Constants.mainMenu
import io.github.yamin8000.twitterscrapper.util.Constants.menuStyle
import io.github.yamin8000.twitterscrapper.util.Constants.t
import io.github.yamin8000.twitterscrapper.util.FileUtils
import kotlinx.coroutines.*

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
            "2" -> {}
            "3" -> {}
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