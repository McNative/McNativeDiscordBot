package org.mcnative.discordbot.commands.setup.betaprocess

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.mcnative.discordbot.McNativeDiscordBot

class DiscordServerSetupBetaProcessCategoryCommand(private val bot: McNativeDiscordBot): Command() {

    private val categories = arrayOf("Testing", "Passed", "Archived")

    init {
        name = "category"
        help = "Setup the beta process categories. Available: Testing, Passed and Archived"
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split(" ")
        if(args.size != 2) {
            event.reply(help)
            return
        }
        val categoryRawIdentifier = args[0].trim()
        val categoryIdentifier = categories.firstOrNull { it.equals(categoryRawIdentifier, true) }
        if(categoryIdentifier == null) {
            event.reply("$categoryRawIdentifier is not an valid category option. Available categories: ${categories.joinToString(", ")}")
            return
        }
        val categoryId = args[1]
        try {
            val category = event.guild.getCategoryById(categoryId)
            if(category == null) {
                sendCategoryNotFound(categoryId, event)
                return
            }
            bot.serverManager.getServer(event.guild).thenAccept {
                it.configuration.categoryIds[categoryIdentifier] = category.idLong
                event.reply("You have successfully set the category id ${category.id} for the category $categoryIdentifier")
            }
        } catch (error: IllegalArgumentException) {
            error.printStackTrace()
            sendCategoryNotFound(categoryId, event)
        }

    }

    private fun sendCategoryNotFound(categoryId: String, event: CommandEvent) {
        event.reply("Can't find a category on your server with the id $categoryId")
    }
}