package io.github.yamin8000.twitterscrapper.modules.crawler

import io.github.yamin8000.twitterscrapper.modules.BaseModule
import io.github.yamin8000.twitterscrapper.util.Menus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class CrawlerModule : BaseModule(Menus.crawlerMenu) {
    override fun run(): Int {
        when (super.run()) {
            0 -> showMenu()
            1 -> crawl()
        }

        run()
        return 0
    }

    private fun crawl() {
        val crawler = Crawler()
        runBlocking {
            withContext(Dispatchers.Default) {
                crawler.crawl()
            }
        }
    }
}
