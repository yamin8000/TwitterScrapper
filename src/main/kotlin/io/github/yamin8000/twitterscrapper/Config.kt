package io.github.yamin8000.twitterscrapper

import io.github.yamin8000.twitterscrapper.util.Constants.DOWNLOAD_FOLDER
import io.github.yamin8000.twitterscrapper.util.Constants.DOWNLOAD_PATH
import io.github.yamin8000.twitterscrapper.util.Constants.errorStyle
import io.github.yamin8000.twitterscrapper.util.Constants.t
import io.github.yamin8000.twitterscrapper.util.FileUtils
import java.io.File

private val configRegex = Regex("(.+=.+\\n*)+")

private const val configPath = "config/config.env"

val configFile = File(configPath)

class Config {

    private val configPairs by lazy { loadConfigPairs() }

    init {
        FileUtils.createDirIfNotExists(DOWNLOAD_PATH)
        FileUtils.createDirIfNotExists("config")
        createConfigFileIfNecessary()
        loadConfigToMemory()
    }

    private fun createConfigFileIfNecessary() {
        when {
            !isConfigFileExists() -> initConfigFile()
            !isConfigFileValid() -> handleInvalidConfigFile()
        }
    }

    private fun initConfigFile() {
        configFile.createNewFile()
        configFile.writeText(
            """
                $DOWNLOAD_FOLDER=$DOWNLOAD_PATH
                """.trimIndent()
        )
    }

    private fun handleInvalidConfigFile() {
        t.println(errorStyle("Config file is invalid, trying to recover it..."))
        val invalidConfigFile = File("$configPath.invalid")
        invalidConfigFile.writeText(configFile.readText())
        val recoveredConfigFile = configRegex.find(configFile.readText())?.value
        if (recoveredConfigFile != null) configFile.writeText(recoveredConfigFile)
        else createConfigFileIfNecessary()
    }

    private fun isConfigFileExists(): Boolean {
        val configFile = File(configPath)
        return configFile.isFile
    }

    private fun isConfigFileValid(): Boolean {
        val configFile = File(configPath)
        val configFileContent = configFile.readText().trim()
        return configRegex.matches(configFileContent)
    }

    fun updateConfigFile(configPair: Pair<String, Any>) {
        configPairs.listIterator().let { iterator ->
            while (iterator.hasNext()) {
                if (iterator.next().first == configPair.first) {
                    iterator.set(configPair.first to configPair.second.toString())
                }
            }
        }
        configFile.writeText(configPairs.joinToString("\n") {
            "${it.first}=${it.second}"
        })
    }

    private fun loadConfigPairs(): MutableList<Pair<String, String>> {
        return configFile.readLines().map { line ->
            val pair = line.trim().split("=").take(2)
            if (pair.size != 2) throw IllegalArgumentException("Invalid config file")
            pair.first() to pair.last()
        }.toMutableList()
    }

    private fun loadConfigToMemory() {
        configPairs.forEach {
            when (it.first) {
                DOWNLOAD_FOLDER -> DOWNLOAD_PATH = it.second
            }
        }
    }
}
