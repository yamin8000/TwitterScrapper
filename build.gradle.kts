plugins {
    kotlin("jvm") version "1.8.0"
    application
}

group = "io.github.yamin8000"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("org.jsoup:jsoup:1.15.3")
}

tasks.test {

}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("MainKt")
}