package dev.expo.analysisbot.commands

import dev.expo.analysisbot.tbadata.ChargedUpMatch.ChargedUpMatch
import dev.expo.analysisbot.tbainterface.Alliance
import dev.expo.analysisbot.tbainterface.JsonToPojo
import dev.expo.analysisbot.tbainterface.TBA
import dev.expo.analysisbot.tbainterface.allianceOf
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import java.awt.Color


object AverageScoredGPAlliance : ListenerAdapter() {
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name != "averagegpalliance" || event.options[0] == null) return
        val teamNumber = event.options[0].asInt

        var avgAuto = 0
        var avgTeleop = 0
        var avgTotal = 0

        val eventKey = event.getOption("eventname")?.asString?.let { TBA.getEventKey(it, 2023) }

        val allMatches = if (eventKey != null) {
            TBA.getJson("team/frc$teamNumber/event/$eventKey/matches")
        } else {
            TBA.getJson("team/frc$teamNumber/matches/2023")
        }.asJsonArray

        val levels = when (event.getOption("row")?.asString) {
            "hybrid" -> 2..2
            "mid" -> 1..1
            "top" -> 0..0
            else -> 0..2
        }

        for (matchData in allMatches) {
            val match = ChargedUpMatch()
            JsonToPojo.populate(matchData.asJsonObject, match)

            val alliance = match.allianceOf(teamNumber)

            val autoScores: Array<List<String>>
            val teleopScores: Array<List<String>>

            if (alliance == Alliance.BLUE) {
                val autoData = match.scoreBreakdown.blue.autoCommunity
                autoScores = arrayOf(autoData.t, autoData.m, autoData.b)

                val teleopData = match.scoreBreakdown.blue.teleopCommunity
                teleopScores = arrayOf(teleopData.t, teleopData.m, teleopData.b)
            } else {
                val autoData = match.scoreBreakdown.red.autoCommunity
                autoScores = arrayOf(autoData.t, autoData.m, autoData.b)

                val teleopData = match.scoreBreakdown.red.teleopCommunity
                teleopScores = arrayOf(teleopData.t, teleopData.m, teleopData.b)
            }

            for (i in levels) {
                for (j in 0..8) {
                    val autoGPState = autoScores[i][j]
                    val teleopGPState = teleopScores[i][j]
                    if (autoGPState != "None") {
                        avgAuto += 1
                        // Counter the fact that the teleop scores count everything scored in auto as well
                        avgTeleop -= 1
                    }
                    if (teleopGPState != "None") {
                        avgTeleop += 1
                        avgTotal += 1
                    }
                }
            }
        }

        val matchesPlayed = allMatches.size()

        val embed = EmbedBuilder()
        embed.addField("Auto", "```fix\n${(avgAuto.toDouble() / matchesPlayed).format()}```", false)
        embed.addField("Teleop", "```fix\n${(avgTeleop.toDouble() / matchesPlayed).format()}```", false)
        embed.addField("Total", "```fix\n${(avgTotal.toDouble() / matchesPlayed).format()}```", false)

        embed.setImage("https://frcavatars.herokuapp.com/get_image?team=$teamNumber")
        embed.setTitle("Alliance GP average for ${TBA.teamNameFromKey("frc$teamNumber")}")
        embed.setColor(Color(0x3fa3cc))

        val description = StringBuilder()
        description.append("Total game pieces scored by alliances including team $teamNumber\n")
        description.append("Matches Played: $matchesPlayed\n")
        description.append("Row Analyzed: ${event.getOption("row")?.asString?.uppercase()}\n")
        event.getOption("eventname")?.asString?.let { description.append("Event Name: $it") }

        embed.setDescription(description.toString())

        event.replyEmbeds(embed.build()).queue()
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        if (!(event.name == "averagegpalliance" && event.focusedOption.name == "eventname")) return

        val events = TBA.getJson("team/frc${event.options[0].asInt}/events/2023/simple").asJsonArray

        val names = mutableListOf<String>()
        for (e in events) {
            names.add(e.asJsonObject.get("name").asString)
        }

        var suggestions = names.filter { it.startsWith(event.focusedOption.value, true) }
        if (suggestions.size > 25) suggestions = suggestions.subList(0, 25)

        event.replyChoiceStrings(suggestions).queue()
    }

    fun getSlashCommand(): SlashCommandData {
        val command = Commands.slash(
            "averagegpalliance", "Gets the total game pieces scored by alliances including a specified team"
        )

        command.addOption(OptionType.INTEGER, "teamnum", "Team Number to get data of", true)
        command.addOptions(
            OptionData(OptionType.STRING, "row", "Row of game pieces", true).addChoices(
                Command.Choice("Hybrid", "hybrid"),
                Command.Choice("Mid", "mid"),
                Command.Choice("Top", "top"),
                Command.Choice("All", "all")
            )
        )
        command.addOption(OptionType.STRING, "eventname", "Name of an event to get data from", false, true)

        return command
    }
}

private fun Double.format(): String {
    var num = this.toString()
    if (num.substring(0, num.indexOf(".")).length == 1) num = "0$num"
    if (num.length > 4) num = num.substring(0, 5)
    if (num.substring(num.indexOf(".")).length == 2) num = "${num}0"
    return num
}