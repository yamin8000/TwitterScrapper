package io.github.yamin8000.twitterscrapper.model

data class User(
    val username: String = "",
    val fullname: String = "",
    val isVerified: Boolean = false,
    val bio: String = "",
    val location: String = "",
    val joinDate: String = "",
    val avatar: String = "",
    val banner: String = "",
    val tweets: Int? = null,
    val following: Int? = null,
    val followers: Int? = null,
    val likes: Int? = null
)