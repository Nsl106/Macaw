package dev.expo.analysisbot

import dev.expo.analysisbot.commands.AverageScoredGPAlliance
import dev.expo.analysisbot.commands.HighestAverageScoredGPAlliance
import dev.expo.analysisbot.commands.Ping
import dev.expo.analysisbot.commands.TotalScoredGPAlliance
import dev.expo.analysisbot.config.Config
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent

object AnalysisBot {
    val config = Config.load()
}

fun main() {
    val jda = JDABuilder.createDefault(AnalysisBot.config.discordToken)
        .addEventListeners(Ping, TotalScoredGPAlliance, AverageScoredGPAlliance, HighestAverageScoredGPAlliance)
        .enableIntents(GatewayIntent.MESSAGE_CONTENT)
        .build()
        .awaitReady()

    jda.getGuildById(1094468997396824194L)?.updateCommands()?.addCommands(
        TotalScoredGPAlliance.getSlashCommand(),
        AverageScoredGPAlliance.getSlashCommand(),
        HighestAverageScoredGPAlliance.getSlashCommand()
    )?.queue()
}