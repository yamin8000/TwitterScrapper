@file:Suppress("SpellCheckingInspection")

plugins {
    kotlin("jvm") version "1.8.20"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("java")
}

group = "io.github.yamin8000"
version = "1.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.11")
    implementation("org.jsoup:jsoup:1.16.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("com.github.ajalt.mordant:mordant:2.1.0")
    implementation("com.soywiz.korlibs.korau:korau-jvm:2.7.0")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("io.github.yamin8000.twitterscrapper.MainKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "io.github.yamin8000.twitterscrapper.MainKt"
    }
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}