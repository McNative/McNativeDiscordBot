package org.mcnative.discordbot.tasks

import net.pretronic.databasequery.api.dsl.find
import net.pretronic.databasequery.api.dsl.update
import org.joda.time.DateTime
import org.mcnative.discordbot.McNativeDiscordBot
import org.mcnative.discordbot.discordserver.Categories
import java.sql.Timestamp

class ArchivedTask(private val bot: McNativeDiscordBot): Runnable {

    override fun run() {
        bot.storage.discordServerBetaProcessesCollection.find {
            where("State", "Passed")
        }.execute().forEach {
            if(it.contains("PassedTime")) {
                val rawPassedTime = it.getObject("PassedTime") as Timestamp
                val passedTime = DateTime(rawPassedTime.time)
                if(passedTime.plusDays(14).isBeforeNow) {
                    this.bot.serverManager.getServer(it.getLong("GuildId")).thenAccept { server ->
                        server.guild?.let { guild ->
                            server.configuration.getCategory(guild, Categories.ARCHIVED)?.let { category ->
                                guild.getTextChannelById(it.getLong("ChannelId"))?.manager?.setParent(category)?.queue()
                            }
                        }
                        bot.storage.discordServerBetaProcessesCollection.update {
                            set("State", "Archived")
                            where("Id", it.getInt("Id"))
                        }.executeAsync()
                    }
                }
            }
        }

    }
}