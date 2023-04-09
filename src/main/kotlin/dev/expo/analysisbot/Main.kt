package dev.expo.analysisbot

import dev.expo.analysisbot.commands.Ping
import dev.expo.analysisbot.config.ConfigGetter
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent

val config = ConfigGetter.load()

fun main() {
    JDABuilder.createDefault(config.discordToken)
        .addEventListeners(Ping())
        .enableIntents(GatewayIntent.MESSAGE_CONTENT)
        .build()
}