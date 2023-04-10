package io.github.yamin8000.twitterscrapper

import com.soywiz.korau.sound.PlaybackParameters
import com.soywiz.korau.sound.infinitePlaybackTimes
import com.soywiz.korau.sound.playAndWait
import com.soywiz.korau.sound.readAudioStream
import com.soywiz.korio.async.launch
import com.soywiz.korio.file.std.toVfs
import io.github.yamin8000.twitterscrapper.modules.MainModule
import io.github.yamin8000.twitterscrapper.modules.settings.Config
import io.github.yamin8000.twitterscrapper.util.Utility.getResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.File

fun main() {
    runCatching {
        CoroutineScope(Dispatchers.IO).launch {
            elonMusk()
        }
    }
    Config()
    MainModule().run()
}

suspend fun elonMusk() {
    val elon = File("elonmusk.mp3")
    if (elon.exists()) {
        elon.toVfs().readAudioStream().playAndWait(
            params = PlaybackParameters(infinitePlaybackTimes)
        )
    }
}
