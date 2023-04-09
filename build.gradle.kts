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
    implementation("net.dv8tion:JDA:5.0.0-beta.3")
}

kotlin { // Extension for easy setup
    jvmToolchain(8) // Target version of generated JVM bytecode
}