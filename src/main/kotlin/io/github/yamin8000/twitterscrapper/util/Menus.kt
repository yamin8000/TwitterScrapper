package io.github.yamin8000.twitterscrapper.util

object Menus {
    val mainMenu = """
        1. Menu
        2. Crawler
        3. Users
        4. Settings
        0. Exit
    """.trimIndent()
    val userMenu = """
        1. Get user(s) info
        2. Save user(s) info
        3. Save users' tweets
        0. Back
    """.trimIndent()

    val crawlerMenu = """
        1. Nested BFS-like crawl
        2. Level one crawl
        0. Back
    """.trimIndent()

    val settingsMenu = """
        1. Show downloads folder
        2. Change downloads folder
        0. Back
    """.trimIndent()
}