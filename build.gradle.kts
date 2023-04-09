plugins {
    kotlin("jvm") version "1.8.10"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("java")
}

group = "io.github.yamin8000"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.11")
    implementation("org.jsoup:jsoup:1.15.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0-Beta")
    implementation("com.github.ajalt.mordant:mordant:2.0.0-beta13")
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("io.github.yamin8000.twitterscrapper.MainKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "io.github.instakiller.MainKt"
    }
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}