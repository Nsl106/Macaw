package dev.expo.analysisbot.commands

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import dev.expo.analysisbot.AnalysisBot
import dev.expo.analysisbot.util.get
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import okhttp3.OkHttpClient
import okhttp3.Request
import java.awt.Color


class TotalScoredGPAlliance : ListenerAdapter() {
    private fun arraysToEmbed(
        array: Array<Array<Double>>, description: String, builder: EmbedBuilder
    ) {
        val sb = StringBuilder()
        sb.append("```fix\n")
        sb.append("${array[0].toFormattedDecimal()}\n")
        sb.append("${array[1].toFormattedDecimal()}\n")
        sb.append("${array[2].toFormattedDecimal()}\n")
        sb.append("```")

        builder.addField(description, sb.toString(), false)
    }


    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot) return

        val message = event.message
        val content = message.contentRaw.split(' ')
        val teamNumber = content[1]

        if (content[0] == "!totalScoredGPAlliance") {
            val emptyArray = arrayOf(
                arrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                arrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                arrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
            )
            val auto = cloneArray(emptyArray)
            val teleop = cloneArray(emptyArray)
            val total = cloneArray(emptyArray)

            val allMatches = if (content.size > 2) {
                pollTBA("team/frc$teamNumber/event/2023${content[2]}/matches")
            } else {
                pollTBA("team/frc$teamNumber/matches/2023")
            }.asJsonArray.asList()


            for (matchNumber in allMatches.indices) {
                val match = allMatches[matchNumber]

                val alliance = getAlliance(match, teamNumber.toInt())

                val scoredGPAuto = match.get("score_breakdown").get(alliance).get("autoCommunity")
                val scoredGPTeleop = match.get("score_breakdown").get(alliance).get("teleopCommunity")

                val autoScores = arrayOf(
                    scoredGPAuto.get("T").asJsonArray.asList(),
                    scoredGPAuto.get("M").asJsonArray.asList(),
                    scoredGPAuto.get("B").asJsonArray.asList()
                )

                for (i in 0..2) {
                    for (j in 0..8) {
                        val gpName = autoScores[i][j].asString
                        if (gpName != "None") {
                            auto[i][j] += 1.0
                            // Counter the fact that the teleop scores count everything scored in auto as well
                            teleop[i][j] -= 1.0
                        }
                    }
                }

                val teleopScores = arrayOf(
                    scoredGPTeleop.get("T").asJsonArray.asList(),
                    scoredGPTeleop.get("M").asJsonArray.asList(),
                    scoredGPTeleop.get("B").asJsonArray.asList()
                )

                for (i in 0..2) {
                    for (j in 0..8) {
                        val gpName = teleopScores[i][j].asString
                        if (gpName != "None") {
                            teleop[i][j] += 1.0
                            total[i][j] += 1.0
                        }
                    }
                }
            }

            val matchesPlayed = allMatches.size

            val embed = EmbedBuilder()
            arraysToEmbed(auto, "Auto", embed)
            arraysToEmbed(teleop, "Tele", embed)
            arraysToEmbed(total, "Total", embed)
            arraysToEmbed(averageArray(total, matchesPlayed), "Avg", embed)

            embed.setImage("https://frcavatars.herokuapp.com/get_image?team=$teamNumber")
            embed.setTitle("Alliance GP totals for team $teamNumber")
            embed.setDescription("All game pieces scored by alliances including team $teamNumber across $matchesPlayed matches")
            embed.setColor(Color(0x3fa3cc))

            val channel = event.channel

            channel.sendMessageEmbeds(embed.build()).queue()
        }
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

    private fun getAlliance(match: JsonElement, teamNumber: Int): String {
        val blueTeamData = match.get("alliances").get("blue").get("team_keys").asJsonArray.asList()
        for (i in blueTeamData.indices) {
            val currentTeam = blueTeamData.elementAt(i)
            if (currentTeam.asString == "frc$teamNumber") return "blue"

        }
        return "red"
    }

    private fun pollTBA(field: String): JsonArray {
        val url = "https://www.thebluealliance.com/api/v3/${field}"

        val client = OkHttpClient()

        val request = Request.Builder().url(url).header("X-TBA-Auth-Key", AnalysisBot.config.tbaApiKey).build()

        val response = client.newCall(request).execute()
        val jsonString = response.body?.string() ?: "null"

        return Gson().fromJson(jsonString, JsonArray::class.java)
    }

}

private fun Array<Double>.toFormattedDecimal(): String {
    val sb = StringBuilder()

    for (i in this) {
        var num = i.toString()
        if (num.substring(0, num.indexOf(".")).length == 1) num = "0$num"
        if (num.length > 4) num = num.substring(0, 5)
        if (num.substring(num.indexOf(".")).length == 2) num = "${num}0"
        sb.append(num)
        sb.append("|")
    }

    return "[${sb.toString().removeSuffix("|")}]"
}
