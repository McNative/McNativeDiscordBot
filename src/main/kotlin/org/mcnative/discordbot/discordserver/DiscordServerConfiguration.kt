package org.mcnative.discordbot.discordserver

import net.dv8tion.jda.api.entities.Category
import net.dv8tion.jda.api.entities.Guild
import net.pretronic.databasequery.api.dsl.update
import net.pretronic.libraries.document.Document
import net.pretronic.libraries.document.type.DocumentFileType
import net.pretronic.libraries.utility.list.ArrayCallbackList
import net.pretronic.libraries.utility.list.CallbackCollection
import net.pretronic.libraries.utility.map.callback.CallbackMap
import net.pretronic.libraries.utility.map.callback.ConcurrentCallbackMap
import org.mcnative.discordbot.McNativeDiscordBot
import java.util.function.BiConsumer
import java.util.function.Consumer

class DiscordServerConfiguration(testerRoleId: Long? = null,
                                 val categoryIds: CallbackMap<String, Long> = ConcurrentCallbackMap(),
                                 val betaProcessResourceIds: CallbackCollection<String> = ArrayCallbackList(),
                                 val changelogNotifierConfigurations: CallbackCollection<ChangelogNotifierConfiguration> = ArrayCallbackList()) {

    lateinit var discordServer: DiscordServer

    var testerRoleId: Long? = testerRoleId
        set(value) {
            field = value
            McNativeDiscordBot.INSTANCE.storage.discordServersCollection.update {
                set("TesterRoleId", value)
                where("GuildId", discordServer.guildId)
            }.executeAsync()
        }


    init {
        val categoryIdsCallback: BiConsumer<String, Long> = BiConsumer { key: String, value: Long ->
            McNativeDiscordBot.INSTANCE.storage.discordServersCollection.update {
                set("CategoryIds", DocumentFileType.JSON.writer.write(Document.newDocument(categoryIds), false))
                where("GuildId", discordServer.guildId)
            }.executeAsync()
        }
        this.categoryIds.setPutCallback(categoryIdsCallback)
        this.categoryIds.setRemoveCallback(categoryIdsCallback)


        val betaProcessResourceIdsCallback: Consumer<String> = Consumer {
            McNativeDiscordBot.INSTANCE.storage.discordServersCollection.update {
                set("BetaProcessResourceIds", DocumentFileType.JSON.writer.write(Document.newDocument(betaProcessResourceIds), false))
                where("GuildId", discordServer.guildId)
            }.executeAsync()
        }
        this.betaProcessResourceIds.setAddCallback(betaProcessResourceIdsCallback)
        this.betaProcessResourceIds.setRemoveCallback(betaProcessResourceIdsCallback)


        val changelogNotifierConfigurationsCallback: Consumer<ChangelogNotifierConfiguration> = Consumer {
            McNativeDiscordBot.INSTANCE.storage.discordServersCollection.update {
                set("ChangelogNotifierConfigurations", DocumentFileType.JSON.writer.write(Document.newDocument(changelogNotifierConfigurations), false))
                where("GuildId", discordServer.guildId)
            }.executeAsync()
        }
        this.changelogNotifierConfigurations.setAddCallback(changelogNotifierConfigurationsCallback)
        this.changelogNotifierConfigurations.setRemoveCallback(changelogNotifierConfigurationsCallback)
    }

    fun getCategory(guild: Guild, identifier: String): Category? {
        val categoryId = categoryIds[identifier]
        return categoryId?.let { guild.getCategoryById(categoryId) }
    }
}