package io.github.yamin8000.twitterscrapper.modules

import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.printTable
import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.table
import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.readMultipleStrings
import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.t
import io.github.yamin8000.twitterscrapper.helpers.UserHelper.getUser
import io.github.yamin8000.twitterscrapper.model.User
import io.github.yamin8000.twitterscrapper.util.Constants.DOWNLOAD_PATH
import io.github.yamin8000.twitterscrapper.util.FileUtils
import io.github.yamin8000.twitterscrapper.util.Menus
import io.github.yamin8000.twitterscrapper.util.Utility.csv
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class UsersModule : BaseModule(Menus.userMenu) {

    private val path = "$DOWNLOAD_PATH/users"

    init {
        FileUtils.createDirIfNotExists(path)
    }

    override fun run(): Int {
        when (super.run()) {
            0 -> showMenu()
            1 -> runBlocking { showUsersInfo() }
            2 -> runBlocking { saveUsersInfo() }
        }

        run()
        return 0
    }

    private suspend fun saveUsersInfo() {
        getUsersInfo().collect {
            File("$path/${it.username.drop(1)}.txt").apply {
                createNewFile()
                appendText(listOf(it).csv())
            }
        }
    }

    private suspend fun showUsersInfo() {
        getUsersInfo().collect { it.printTable() }
    }

    private suspend fun getUsersInfo(): Flow<User> = flow {
        readMultipleStrings("User").forEach { username ->
            getUser(username)?.let { emit(it) }
        }
    }
}