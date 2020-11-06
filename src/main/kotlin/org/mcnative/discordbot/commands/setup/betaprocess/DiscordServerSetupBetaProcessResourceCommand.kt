package org.mcnative.discordbot.commands.setup.betaprocess

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.Permission
import org.mcnative.discordbot.McNativeDiscordBot

class DiscordServerSetupBetaProcessResourceCommand(private val bot: McNativeDiscordBot): Command() {

    init {
        name = "resource"
        help = "Add and remove resources from the beta process."
        userPermissions = arrayOf(Permission.ADMINISTRATOR)
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split(" ")
        val server = bot.serverManager.getServer(event.guild)
        if(args.isNotEmpty() && args[0].equals("list", true)) {
            server.thenAccept {
                event.reply("You have configured following resources to the beta process:${it.configuration.betaProcessResourceIds.joinToString("\n- ", "\n- ")}")
            }
            return
        } else if(args.size != 2) {
            event.reply(help)
            return
        }
        val resourceId = args[1]
        when(args[0].toLowerCase()) {
            "add" -> {
                server.thenAccept {
                    it.configuration.betaProcessResourceIds.add(resourceId)
                    event.reply("You have added the resource with the id $resourceId to the beta process")
                }
                return
            }
            "remove" -> {
                server.thenAccept {
                    it.configuration.betaProcessResourceIds.remove(resourceId)
                    event.reply("You have removed the resource with the id $resourceId to the beta process")
                }
                return
            }
            else -> {
                event.reply(help)
            }
        }
    }
}