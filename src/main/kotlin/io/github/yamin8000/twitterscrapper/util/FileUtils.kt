package io.github.yamin8000.twitterscrapper.util

import java.io.File

object FileUtils {

    fun createDirIfNotExists(pathname: String): Boolean {
        val dir = File(pathname.trim())
        if (!dir.exists()) dir.mkdirs()
        return dir.exists()
    }
}