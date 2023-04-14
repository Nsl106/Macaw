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
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import java.awt.Color


object TotalScoredGPAlliance : ListenerAdapter() {
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name != "totalgpalliance" || event.options[0] == null) return

        val teamNumber = event.options[0].asInt

        val emptyArray = arrayOf(
            arrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            arrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            arrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        )
        val auto = cloneArray(emptyArray)
        val teleop = cloneArray(emptyArray)
        val total = cloneArray(emptyArray)

        val eventKey = event.getOption("eventname")?.asString?.let { TBA.getEventKey(it, 2023) }

        val allMatches = if (eventKey != null) {
            TBA.getJson("team/frc$teamNumber/event/$eventKey/matches")
        } else {
            TBA.getJson("team/frc$teamNumber/matches/2023")
        }.asJsonArray



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

            for (i in 0..2) {
                for (j in 0..8) {
                    val autoGPState = autoScores[i][j]
                    val teleopGPState = teleopScores[i][j]
                    if (autoGPState != "None") {
                        auto[i][j] += 1.0
                        // Counter the fact that the teleop scores count everything scored in auto as well
                        teleop[i][j] -= 1.0
                    }
                    if (teleopGPState != "None") {
                        teleop[i][j] += 1.0
                        total[i][j] += 1.0
                    }
                }
            }
        }

        val matchesPlayed = allMatches.size()

        val embed = EmbedBuilder()
        arrayToEmbed(auto, "Auto", embed)
        arrayToEmbed(teleop, "Tele", embed)
        arrayToEmbed(total, "Total", embed)
        arrayToEmbed(averageArray(total, matchesPlayed), "Avg", embed)

        embed.setImage("https://frcavatars.herokuapp.com/get_image?team=$teamNumber")
        embed.setTitle("Alliance GP totals for team $teamNumber")
        embed.setDescription("All game pieces scored by alliances including team $teamNumber across $matchesPlayed matches")
        embed.setColor(Color(0x3fa3cc))

        event.replyEmbeds(embed.build()).queue()
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        if (!(event.name == "totalgpalliance" && event.focusedOption.name == "eventname")) return

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
            "totalgpalliance", "Gets the total game pieces scored by alliances including a specified team"
        )

        command.addOption(OptionType.INTEGER, "teamnum", "Team Number to get data of", true)
        command.addOption(OptionType.STRING, "eventname", "Name of an event to get data from", false, true)
        return command
    }

    private fun arrayToEmbed(array: Array<Array<Double>>, description: String, builder: EmbedBuilder) {
        val sb = StringBuilder()
        sb.append("```fix\n")
        sb.append("${doubleArrayToString(array[0])}\n")
        sb.append("${doubleArrayToString(array[1])}\n")
        sb.append("${doubleArrayToString(array[2])}\n")
        sb.append("```")

        builder.addField(description, sb.toString(), false)
    }

    private fun cloneArray(array: Array<Array<Double>>): Array<Array<Double>> {
        val output = arrayOf<Array<Double>>(arrayOf(), arrayOf(), arrayOf())
        for (i in array.indices) {
            output[i] = array[i].copyOf()
        }
        return output
    }

    private fun averageArray(array: Array<Array<Double>>, matchCount: Int): Array<Array<Double>> {
        val output = cloneArray(array)
        for (i in output.indices) {
            for (j in output[i].indices) {
                output[i][j] /= matchCount.toDouble()
            }
        }
        return output
    }

    private fun doubleArrayToString(array: Array<Double>): String {
        val sb = StringBuilder()

        for (i in array) {
            var num = i.toString()
            if (num.substring(0, num.indexOf(".")).length == 1) num = "0$num"
            if (num.length > 4) num = num.substring(0, 5)
            if (num.substring(num.indexOf(".")).length == 2) num = "${num}0"
            sb.append(num)
            sb.append("|")
        }

        return "[${sb.toString().removeSuffix("|")}]"
    }

}