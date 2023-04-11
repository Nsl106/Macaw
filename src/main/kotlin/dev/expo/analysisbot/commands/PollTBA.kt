package dev.expo.analysisbot.commands

import dev.expo.analysisbot.AnalysisBot
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import okhttp3.OkHttpClient
import okhttp3.Request

class PollTBA : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot) return

        val message = event.message
        val content = message.contentRaw.split(' ')


        if (content[0] == "!pollTBA") {
            val channel = event.channel
            var response = pollTBA(content[1])

            if (response.length > 2000) {
                response = response.substring(0, 2000)
            }

            channel.sendMessage(response).queue()
        }
    }

    private fun pollTBA(field: String): String {
        val url = "https://www.thebluealliance.com/api/v3/${field}"

        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .header("X-TBA-Auth-Key", AnalysisBot.config.tbaApiKey)
            .build()

        val response = client.newCall(request).execute()

        return response.body?.string() ?: "null"
    }
}