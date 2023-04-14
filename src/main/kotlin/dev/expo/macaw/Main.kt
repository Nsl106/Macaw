package dev.expo.macaw

import dev.expo.macaw.commands.AverageScoredGPAlliance
import dev.expo.macaw.commands.HighestAverageScoredGPAlliance
import dev.expo.macaw.commands.Ping
import dev.expo.macaw.commands.TotalScoredGPAlliance
import dev.expo.macaw.config.Config
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent

object Macaw {
    val config = Config.load()
}

fun main() {
    val jda = JDABuilder.createDefault(Macaw.config.discordToken)
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