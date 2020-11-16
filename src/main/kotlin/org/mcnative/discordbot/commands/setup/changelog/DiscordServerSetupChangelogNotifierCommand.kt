package org.mcnative.discordbot.commands.setup.changelog

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.Permission
import net.pretronic.libraries.utility.GeneralUtil
import org.mcnative.discordbot.discordserver.ChangelogNotifierConfiguration
import org.mcnative.discordbot.getDiscordServer

class DiscordServerSetupChangelogNotifierCommand: Command() {

    init {
        name = "changelogNotifier"
        help = "Control the changelog notifiers"
        userPermissions = arrayOf(Permission.ADMINISTRATOR)
        children = arrayOf(Add(), Remove(), List())
    }

    override fun execute(event: CommandEvent?) {
        TODO("Not yet implemented")
    }

    private class Add: Command() {

        init {
            name = "add"
            help = "Add a changelog notifier"
            userPermissions = arrayOf(Permission.ADMINISTRATOR)
        }

        override fun execute(event: CommandEvent) {
            val args = event.args.split(" ")
            if(args.size != 3) {
                event.reply(help)
                return
            }
            val rawChannelId = args[0]
            if(!GeneralUtil.isNaturalNumber(rawChannelId)) {
                event.reply("Please use a valid channelId")
                return
            }
            val channelId = rawChannelId.toLong()
            val resourceId = args[1]

            val qualifier = args[2] //Maybe check in future

            val server = event.getDiscordServer()
            server.configuration.changelogNotifierConfigurations.add(ChangelogNotifierConfiguration(channelId, resourceId, qualifier))
            event.reply("You have successfully added a changelog notifier for resourceId $resourceId to channel with id $channelId and qualifier $qualifier")
        }
    }

    private class Remove: Command() {

        init {
            name = "remove"
            help = "Remove a changelog notifier"
            userPermissions = arrayOf(Permission.ADMINISTRATOR)
        }

        override fun execute(event: CommandEvent) {
            val args = event.args.split(" ")
            if(args.size != 2) {
                event.reply(help)
                return
            }
            val rawChannelId = args[0]
            if(!GeneralUtil.isNaturalNumber(rawChannelId)) {
                event.reply("Please use a valid channelId")
                return
            }
            val channelId = rawChannelId.toLong()
            val resourceId = args[1]

            val qualifier = args[2] //Maybe check in future

            val server = event.getDiscordServer()
            server.configuration.changelogNotifierConfigurations.removeIf {
                it.resourceId == resourceId && it.channelId == channelId && it.qualifier == qualifier
            }
            event.reply("You have successfully removed a changelog notifier for resourceId $resourceId to channel with id $channelId and qualifier $qualifier")
        }
    }

    private class List: Command() {

        init {
            name = "list"
            help = "List all changelog notifiers"
            userPermissions = arrayOf(Permission.ADMINISTRATOR)
        }

        override fun execute(event: CommandEvent) {
            event.reply("You have configured following resources to the beta process:${event.getDiscordServer().configuration.changelogNotifierConfigurations
                    .joinToString("\n- ", "\n- ", transform = { "ChannelId: ${it.channelId} | ResourceId: ${it.resourceId} | Qualifier: ${it.qualifier}"})}")
        }

    }
}