package io.github.yamin8000.twitterscrapper.helpers

import io.github.yamin8000.twitterscrapper.web.get
import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.errorStyle
import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.t
import io.github.yamin8000.twitterscrapper.model.User
import io.github.yamin8000.twitterscrapper.util.Constants.instances
import io.github.yamin8000.twitterscrapper.util.Utility.sanitizeNum
import io.github.yamin8000.twitterscrapper.util.Utility.sanitizeUsername
import org.jsoup.Jsoup
import kotlin.random.Random
import kotlin.random.nextInt

object UserInfoHelper {
    @Throws(Exception::class)
    suspend fun getUser(
        username: String,
        base: String = instances.first()
    ): User? {
        val response = get("${base}${username.sanitizeUsername()}")
        return if (response.isSuccessful) parseUser(response.body.string())
        else getUserFailedRequest(username, response.code)
    }

    @Throws(Exception::class)
    private suspend fun getUserFailedRequest(
        username: String,
        httpCode: Int
    ): User? {
        val temp = instances.drop(0)
        if (temp.isNotEmpty()) return getUser(username, instances[Random.nextInt(instances.indices)])
        else throw Exception("Fetching info for user: $username failed with $httpCode")
    }

    private fun parseUser(
        html: String
    ): User? {
        return try {
            val doc = Jsoup.parse(html)
            User(
                username = doc.selectFirst("a[class^=profile-card-username]")?.attr("title") ?: "",
                fullname = doc.selectFirst("a[class^=profile-card-fullname]")?.attr("title") ?: "",
                isVerified = doc.selectFirst("div[class^=profile-card-tabs-name] span[class^=icon-ok verified-icon]") != null,
                bio = doc.selectFirst("div[class^=profile-bio] > p")?.text() ?: "",
                location = doc.select("div[class^=profile-location] > span").getOrNull(1)?.text() ?: "",
                joinDate = doc.selectFirst("div[class^=profile-joindate] > span[title]")?.attr("title") ?: "",
                avatar = instances.first().dropLast(1) + doc.selectFirst("a[class^=profile-card-avatar]")?.attr("href"),
                banner = instances.first().dropLast(1) + doc.selectFirst("div[class^=profile-banner] a")?.attr("href"),
                tweets = doc.selectFirst("li[class^=posts] span[class^=profile-stat-num]")?.text().sanitizeNum(),
                following = doc.selectFirst("li[class^=following] > span[class^=profile-stat-num]")?.text()
                    .sanitizeNum(),
                followers = doc.selectFirst("li[class^=followers] > span[class^=profile-stat-num]")?.text()
                    .sanitizeNum(),
                likes = doc.selectFirst("li[class^=likes] span[class^=profile-stat-num]")?.text().sanitizeNum()
            )
        } catch (e: Exception) {
            t.println(errorStyle(e.stackTraceToString()))
            null
        }
    }
}