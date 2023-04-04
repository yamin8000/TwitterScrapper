package io.github.yamin8000.twitterscrapper.util

object Constants {
    const val TWITTER_PROFILE_REGEX_RULE = "@[a-zA-Z0-9_]+"
    val twitterProfileRegex = Regex(TWITTER_PROFILE_REGEX_RULE)

    const val DOWNLOAD_PATH_KEY = "DOWNLOAD_PATH"
    var DOWNLOAD_PATH = "c:\\TwitterScrapper"

    var DEFAULT_TWEETS_LIMIT = 500

    val FAILED_REQUEST_DELAY = 50L..500L

    const val ERROR_503 = "503 Service Temporarily Unavailable"

    val instances = listOf(
        "https://nitter.net/",
        "https://nitter.lacontrevoie.fr/",
        "https://nitter.fdn.fr/",
        "https://nitter.kavin.rocks/",
        "https://nitter.unixfox.eu/",
        "https://nitter.domain.glass/",
        "https://nitter.moomoo.me/",
        "https://nitter.grimneko.de/",
        "https://nitter.fly.dev/",
        "https://nitter.weiler.rocks/",
        "https://nitter.sethforprivacy.com/",
        "https://nitter.cutelab.space/",
        "https://nitter.mint.lgbt/",
        "https://nitter.esmailelbob.xyz/",
        "https://nitter.winscloud.net/",
        "https://nitter.tiekoetter.com/",
        "https://nitter.spaceint.fr/",
        "https://nitter.poast.org/",
        "https://nitter.privacydev.net/",
        "https://nitter.kylrth.com/",
        "https://nitter.foss.wtf/",
        "https://nitter.priv.pw/",
        "https://nitter.tokhmi.xyz/",
        "https://nitter.catalyst.sx/",
        "https://nitter.projectsegfau.lt/",
        "https://nitter.slipfox.xyz/",
        "https://nitter.soopy.moe/",
        "https://nitter.qwik.space/",
        "https://nitter.rawbit.ninja/",
        "https://nitter.privacytools.io/",
        "https://nitter.sneed.network/",
        "https://nitter.manasiwibi.com/",
        "https://nitter.smnz.de/",
        "https://nitter.twei.space/",
        "https://nitter.inpt.fr/",
        "https://nitter.caioalonso.com/",
        "https://nitter.nicfab.eu/",
        "https://nitter.hostux.net/",
        "https://nitter.adminforge.de/",
        "https://nitter.platypush.tech/",
        "https://nitter.pufe.org/",
        "https://nitter.arcticfoxes.net/",
        "https://nitter.kling.gg/",
        "https://nitter.ktachibana.party/",
        "https://nitter.riverside.rocks/",
        "https://nitter.girlboss.ceo/",
        "https://nitter.lunar.icu/",
        "https://nitter.freedit.eu/",
        "https://nitter.librenode.org/",
        "https://nitter.plus.st/",
        "https://nitter.tux.pizza/",
    )
}