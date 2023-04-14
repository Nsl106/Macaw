package dev.expo.macaw.config

import java.io.File
import java.io.FileInputStream
import java.util.Properties

object Config {
    fun load(): ConfigData {
        val properties = Properties()
        properties.load(FileInputStream(File("src/main/kotlin/dev/expo/macaw/config.properties")))

        return ConfigData(
            discordToken = properties.getProperty("discordToken"),
            tbaApiKey = properties.getProperty("tbaApiKey")
        )
    }
}

data class ConfigData(
    val discordToken: String,
    val tbaApiKey: String
)
