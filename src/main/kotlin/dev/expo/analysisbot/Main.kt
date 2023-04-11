package dev.expo.analysisbot

import dev.expo.analysisbot.commands.Ping
import dev.expo.analysisbot.commands.PollTBA
import dev.expo.analysisbot.commands.TotalScoredGPAlliance
import dev.expo.analysisbot.config.ConfigGetter
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.requests.GatewayIntent

object AnalysisBot {
    val config = ConfigGetter.load()
}

fun main() {
    val JDA = JDABuilder.createDefault(AnalysisBot.config.discordToken)
        .addEventListeners(Ping(), PollTBA(), TotalScoredGPAlliance())
        .enableIntents(GatewayIntent.MESSAGE_CONTENT)
        .build()

}