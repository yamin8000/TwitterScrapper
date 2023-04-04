package io.github.yamin8000.twitterscrapper.modules

import io.github.yamin8000.twitterscrapper.modules.crawler.CrawlerModule
import io.github.yamin8000.twitterscrapper.modules.settings.SettingsModule
import io.github.yamin8000.twitterscrapper.util.Menus

class MainModule : BaseModule(Menus.mainMenu) {
    override fun run(): Int {
        when (super.run()) {
            0 -> return 0
            1 -> showMenu()
            2 -> CrawlerModule().run()
            3 -> UsersModule().run()
            4 -> SettingsModule().run()
        }

        run()
        return 0
    }
}