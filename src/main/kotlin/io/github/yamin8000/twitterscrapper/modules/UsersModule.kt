package io.github.yamin8000.twitterscrapper.modules

import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.printTable
import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.readInteger
import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.readMultipleStrings
import io.github.yamin8000.twitterscrapper.helpers.UserInfoHelper.getUser
import io.github.yamin8000.twitterscrapper.helpers.UserTweetsRequest
import io.github.yamin8000.twitterscrapper.model.User
import io.github.yamin8000.twitterscrapper.util.Constants.DOWNLOAD_PATH
import io.github.yamin8000.twitterscrapper.util.FileUtils
import io.github.yamin8000.twitterscrapper.util.Menus
import io.github.yamin8000.twitterscrapper.util.Utility.csv
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import java.io.File

class UsersModule : BaseModule(Menus.userMenu) {

    private val path = "$DOWNLOAD_PATH/users"

    init {
        FileUtils.createDirIfNotExists(path)
    }

    override fun run(): Int {
        when (super.run()) {
            0 -> return showMenu()
            1 -> runBlocking { showUsersInfo() }
            2 -> runBlocking { saveUsersInfo() }
            3 -> runBlocking {
                getUserTweets()
            }
        }

        run()
        return 0
    }

    private suspend fun getUserTweets() {
        val users = readMultipleStrings("Username")
        val limit = readInteger(
            message = "Enter number of tweets",
            range = 1..1000
        )
        users.forEach { user ->
            UserTweetsRequest(user, limit).get().forEach {
                println(it)
            }
        }
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
        readMultipleStrings("Username").forEach { username ->
            getUser(username)?.let { emit(it) }
        }
    }
}