package dev.expo.macaw

import dev.expo.macaw.commands.AverageScoredGPAlliance
import dev.expo.macaw.commands.HighestAverageScoredGPAlliance
import dev.expo.macaw.commands.Ping
import dev.expo.macaw.commands.TotalScoredGPAlliance
import dev.expo.macaw.config.Config
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent

object Macaw {
    val config = Config.getConfig()
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

//    JsonInterface.generateClass("match/2023azgl_f1m1", "ChargedUpMatch")
//    JsonInterface.generateClass("team/frc498", "Team")
}