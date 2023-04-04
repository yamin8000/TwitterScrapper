package io.github.yamin8000.twitterscrapper

import io.github.yamin8000.twitterscrapper.modules.MainModule
import io.github.yamin8000.twitterscrapper.modules.settings.Config

fun main() {
    Config()
    MainModule().run()
}