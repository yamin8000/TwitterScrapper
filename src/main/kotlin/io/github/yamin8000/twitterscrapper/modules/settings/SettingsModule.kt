package io.github.yamin8000.twitterscrapper.modules.settings

import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.infoStyle
import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.readSingleString
import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.t
import io.github.yamin8000.twitterscrapper.modules.BaseModule
import io.github.yamin8000.twitterscrapper.util.Constants.DOWNLOAD_PATH
import io.github.yamin8000.twitterscrapper.util.Constants.DOWNLOAD_PATH_KEY
import io.github.yamin8000.twitterscrapper.util.Menus

class SettingsModule : BaseModule(Menus.settingsMenu) {

    private val config = Config()

    override fun run(): Int {
        when (super.run()) {
            0 -> return 0
            1 -> changeDownloadsFolder()
        }

        run()
        return 0
    }

    private fun changeDownloadsFolder() {
        t.println(infoStyle("current download folder is: $DOWNLOAD_PATH"))
        DOWNLOAD_PATH = readSingleString(DOWNLOAD_PATH_KEY)
        config.updateConfigFile(DOWNLOAD_PATH_KEY to DOWNLOAD_PATH)
    }
}
