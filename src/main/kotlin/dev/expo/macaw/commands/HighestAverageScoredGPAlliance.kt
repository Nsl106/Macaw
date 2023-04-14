package dev.expo.macaw.commands

import dev.expo.macaw.tbadata.ChargedUpMatch.ChargedUpMatch
import dev.expo.macaw.tbainterface.Alliance
import dev.expo.macaw.tbainterface.JsonInterface
import dev.expo.macaw.tbainterface.TBA
import dev.expo.macaw.tbainterface.allianceOf
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


object HighestAverageScoredGPAlliance : ListenerAdapter() {
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name != "highestaveragescoredgpalliance" || event.options[0] == null) return
        event.deferReply().queue()

        val eventKey = event.getOption("eventname")?.asString?.let { TBA.getEventKey(it, 2023) }

        val allTeams = TBA.getJson("event/$eventKey/teams/keys")

        val levels = when (event.getOption("row")?.asString) {
            "hybrid" -> 2..2
            "mid" -> 1..1
            "top" -> 0..0
            else -> 0..2
        }


        val teamToScore = mutableMapOf<Int, Double>()

        for (teamData in allTeams.asJsonArray.asList()) {
            // Substring removes the leading 'frc'
            val teamNumber = teamData.asString.substring(3).toInt()
            val allMatches = TBA.getJson("team/frc$teamNumber/matches/2023").asJsonArray
            var totalScores = 0

            for (matchData in allMatches) {
                val match = ChargedUpMatch()
                JsonInterface.populate(matchData.asJsonObject, match)

                val alliance = match.allianceOf(teamNumber)

                val scores = if (alliance == Alliance.BLUE) {
                    val data = match.scoreBreakdown?.blue?.teleopCommunity ?: continue
                    arrayOf(data.t, data.m, data.b)
                } else {
                    val data = match.scoreBreakdown?.red?.teleopCommunity ?: continue
                    arrayOf(data.t, data.m, data.b)
                }

                for (i in levels) {
                    for (j in 0..8) {
                        if (scores[i][j] != "None") {
                            totalScores += 1
                        }
                    }
                }
            }
            val matchesPlayed = allMatches.size()

            val averageScore = totalScores.toDouble() / matchesPlayed
            teamToScore[teamNumber] = averageScore
        }

        val sortedScores = teamToScore.toList().sortedBy { (key, value) -> value }.reversed()
        val quantity = (event.getOption("quantity")?.asInt ?: 10)

        val displayString = StringBuilder()
        displayString.append("```fix\n")
        for (i in 0 until quantity) {
            displayString.append("#${i+1}:${space(1-(i+1).toString().length)} Team ${sortedScores[i].first.format(4)} - ${sortedScores[i].second.format()}\n")
        }
        displayString.append("```")

        val embed = EmbedBuilder()

        val eventName = event.getOption("eventname")?.asString
        embed.setTitle("Top $quantity teams competing in the $eventName")
        embed.setColor(Color(0x3fa3cc))

        val description = StringBuilder()
        description.append("Top $quantity out of ${allTeams.asJsonArray.size()} teams competing in the $eventName based on quantity of average alliance scored game pieces\n")
        description.append("Row Analyzed: ${event.getOption("row")?.asString?.uppercase()}\n")
        description.append("Event Name: $eventName\n")
        description.append(displayString.toString())

        embed.setDescription(description.toString())

        event.hook.sendMessageEmbeds(embed.build()).queue()
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        if (!(event.name == "highestaveragescoredgpalliance" && event.focusedOption.name == "eventname")) return

        val events = TBA.getJson("events/2023/simple").asJsonArray

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
            "highestaveragescoredgpalliance",
            "Gets the total game pieces scored by alliances including a specified team"
        )
        command.addOption(OptionType.STRING, "eventname", "Name of an event to get data from", true, true)

        command.addOptions(
            OptionData(OptionType.STRING, "row", "Row of game pieces", true).addChoices(
                Command.Choice("Hybrid", "hybrid"),
                Command.Choice("Mid", "mid"),
                Command.Choice("Top", "top"),
                Command.Choice("All", "all")
            )
        )

        command.addOption(OptionType.INTEGER, "quantity", "Number of top scores", false)

        return command
    }

    private fun space(quantity: Int): String {
        var str = ""
        for (i in 0..quantity)
            str = "$str "
        return str
    }
}

private fun Double.format(): String {
    var num = this.toString()
    if (num.substring(0, num.indexOf(".")).length == 1) num = "0$num"
    if (num.length > 4) num = num.substring(0, 5)
    if (num.substring(num.indexOf(".")).length == 2) num = "${num}0"
    return num
}

private fun Int.format(spacing: Int): String {
    var num = this.toString()
    for (i in 0..(spacing - num.length)) {
        num = "$num "
    }
    return num
}