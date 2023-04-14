package dev.expo.analysisbot.tbainterface

import com.google.gson.Gson
import com.google.gson.JsonElement
import dev.expo.analysisbot.AnalysisBot
import dev.expo.analysisbot.tbadata.ChargedUpMatch.ChargedUpMatch
import dev.expo.analysisbot.tbadata.Team.Team
import okhttp3.OkHttpClient
import okhttp3.Request

object TBA {
    fun getEventKey(name: String, year: Int): String? {
        val nameToKey = mutableMapOf<String, String>()
        val events = getJson("events/$year/simple").asJsonArray

        for (e in events) {
            nameToKey[e.asJsonObject.get("name").asString] = e.asJsonObject.get("key").asString
        }

        return nameToKey[name]
    }

    fun getJsonString(field: String): String {
        val url = "https://www.thebluealliance.com/api/v3/${field}"

        val client = OkHttpClient()

        val request = Request.Builder().url(url).header("X-TBA-Auth-Key", AnalysisBot.config.tbaApiKey).build()

        val response = client.newCall(request).execute()

        return response.body?.string() ?: "null"
    }

    fun getJson(field: String): JsonElement {
        return Gson().fromJson(getJsonString(field), JsonElement::class.java)
    }

    fun teamNameFromKey(key: String): String {
        val team = Team()
        JsonToPojo.fill("team/$key", team)
        return team.nickname
    }
}

enum class Alliance { RED, BLUE }

fun ChargedUpMatch.allianceOf(teamNumber: Int): Alliance {
    val blueAlliance = alliances.blue.teamKeys
    for (team in blueAlliance) {
        if (team == "frc$teamNumber") return Alliance.BLUE
    }
    return Alliance.RED
}