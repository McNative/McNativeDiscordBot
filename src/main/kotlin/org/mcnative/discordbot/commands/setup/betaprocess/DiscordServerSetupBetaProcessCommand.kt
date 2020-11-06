package org.mcnative.discordbot.commands.setup.betaprocess

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.Permission
import org.mcnative.discordbot.McNativeDiscordBot

class DiscordServerSetupBetaProcessCommand(bot: McNativeDiscordBot): Command() {

    init {
        name = "betaProcess"
        help = "Setup the beta process"
        userPermissions = arrayOf(Permission.ADMINISTRATOR)
        children = arrayOf(DiscordServerSetupBetaProcessCategoryCommand(bot), DiscordServerSetupBetaProcessResourceCommand(bot))
    }

    override fun execute(event: CommandEvent) {

    }


}