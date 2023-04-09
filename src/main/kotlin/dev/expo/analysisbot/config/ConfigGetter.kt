package dev.expo.analysisbot.config

import java.io.File
import java.io.FileInputStream
import java.util.Properties

object ConfigGetter {
    fun load(): Config {
        val properties = Properties()
        properties.load(FileInputStream(File("src/main/kotlin/dev/expo/analysisbot/config.properties")))

        return Config(
            discordToken = properties.getProperty("discordToken"),
            tbaApiKey = properties.getProperty("tbaApiKey")
        )
    }
}