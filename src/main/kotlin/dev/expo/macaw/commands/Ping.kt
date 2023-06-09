package dev.expo.macaw.commands

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter


object Ping : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot) return

        val message = event.message
        val content = message.contentRaw

        if (content == "!ping") {
            val channel = event.channel
            channel.sendMessage("Pong!").queue()
        }
    }
}