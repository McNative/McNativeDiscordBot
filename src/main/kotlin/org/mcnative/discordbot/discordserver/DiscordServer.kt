package org.mcnative.discordbot.discordserver

import net.dv8tion.jda.api.entities.Guild
import net.pretronic.databasequery.api.dsl.update
import net.pretronic.libraries.document.Document
import net.pretronic.libraries.document.type.DocumentFileType
import net.pretronic.libraries.utility.list.ArrayCallbackList
import net.pretronic.libraries.utility.list.CallbackCollection
import org.mcnative.discordbot.McNativeDiscordBot
import java.util.function.Consumer

class DiscordServer(val guildId: Long,
                    val configuration: DiscordServerConfiguration = DiscordServerConfiguration(),
                    val sentChangelogNotifications: CallbackCollection<ChangelogNotification> = ArrayCallbackList()) {

    val guild: Guild?
        get() = McNativeDiscordBot.INSTANCE.jda.getGuildById(guildId)

    init {
        configuration.discordServer = this

        val sentChangelogNotificationsCallback: Consumer<ChangelogNotification> = Consumer {
            McNativeDiscordBot.INSTANCE.storage.discordServersCollection.update {
                set("SentChangelogNotifications", DocumentFileType.JSON.writer.write(Document.newDocument(sentChangelogNotifications), false))
                where("GuildId", guildId)
            }.executeAsync()
        }

        this.sentChangelogNotifications.setAddCallback(sentChangelogNotificationsCallback)
        this.sentChangelogNotifications.setRemoveCallback(sentChangelogNotificationsCallback)
    }
}