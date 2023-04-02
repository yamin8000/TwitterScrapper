plugins {
    kotlin("jvm") version "1.8.10"
    application
}

group = "io.github.yamin8000"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.10")
    implementation("org.jsoup:jsoup:1.15.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0-Beta")
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("MainKt")
}