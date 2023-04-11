plugins {
    id("com.github.johnrengelman.shadow") version "5.2.0"
    kotlin("jvm") version "1.8.20"
    application
}

application.mainClass.set("dev.expo.analysisbot.MainKt")

version "1.0"

repositories {
    mavenCentral()

    maven {
        name = "Jitpack"
        url = uri("https://jitpack.io")
    }
    gradlePluginPortal()
}

dependencies {
    implementation("net.dv8tion:JDA:5.0.0-beta.3") // Java discord api (JDA)
    implementation("com.squareup.okhttp3:okhttp:4.10.0") // Http client (OkHttp)
    implementation("com.google.code.gson:gson:2.10.1") // JSON library (gson)
}

kotlin { // Extension for easy setup
    jvmToolchain(8) // Target version of generated JVM bytecode
}