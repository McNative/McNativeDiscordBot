package org.mcnative.discordbot.discordserver

import net.dv8tion.jda.api.entities.Guild
import org.mcnative.discordbot.McNativeDiscordBot

class DiscordServer(val guildId: Long,
                    val configuration: DiscordServerConfiguration = DiscordServerConfiguration()) {

    val guild: Guild?
        get() = McNativeDiscordBot.INSTANCE.jda.getGuildById(guildId)

    init {
        configuration.discordServer = this
    }
}