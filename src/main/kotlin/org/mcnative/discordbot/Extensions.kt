package org.mcnative.discordbot

import com.jagrosh.jdautilities.command.CommandEvent
import org.mcnative.discordbot.discordserver.DiscordServer

fun CommandEvent.getDiscordServer(): DiscordServer {
    return McNativeDiscordBot.INSTANCE.serverManager.getServer(this.guild)
}