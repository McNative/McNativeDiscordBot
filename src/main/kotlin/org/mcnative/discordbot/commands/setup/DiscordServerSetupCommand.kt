package org.mcnative.discordbot.commands.setup

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.Permission
import org.mcnative.discordbot.McNativeDiscordBot
import org.mcnative.discordbot.commands.ADMINISTRATION_CATEGORY
import org.mcnative.discordbot.commands.setup.betaprocess.DiscordServerSetupBetaProcessCommand
import org.mcnative.discordbot.commands.setup.changelog.DiscordServerSetupChangelogNotifierCommand

class DiscordServerSetupCommand(bot: McNativeDiscordBot): Command() {

    init {
        name = "setup"
        help = "Setup the McNativeDiscordBot"
        userPermissions = arrayOf(Permission.ADMINISTRATOR)
        category = ADMINISTRATION_CATEGORY
        children = arrayOf(DiscordServerSetupBetaProcessCommand(bot), DiscordServerSetupChangelogNotifierCommand())
    }

    override fun execute(event: CommandEvent) {

    }
}