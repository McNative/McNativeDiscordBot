package org.mcnative.discordbot.tasks

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import net.pretronic.databasequery.api.dsl.find
import net.pretronic.databasequery.api.dsl.insert
import net.pretronic.databasequery.api.dsl.update
import net.pretronic.databasequery.api.query.result.QueryResult
import net.pretronic.databasequery.api.query.result.QueryResultEntry
import net.pretronic.libraries.document.type.DocumentFileType
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.mcnative.discordbot.McNativeDiscordBot
import org.mcnative.discordbot.discordserver.Categories
import org.mcnative.discordbot.discordserver.DiscordServer
import org.mcnative.discordbot.requests.McNativeHttpRequestBuilder
import org.mcnative.discordbot.requests.ResourceVersion
import org.mcnative.discordbot.requests.ResourceVersionStatus
import java.awt.Color
import java.lang.StringBuilder

class BetaProcessTask(private val bot: McNativeDiscordBot): Runnable {

    private var lastContact: DateTime = DateTime.now().minusMinutes(1)

    override fun run() {
        println(DateTime.now().toDateTimeISO().toString(DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")) + " BETA")
        val now = DateTime.now()
        this.bot.serverManager.servers.cachedObjects.forEach {
            it.configuration.betaProcessResourceIds.forEach { resourceId ->
                handleDeltaRequest(it, resourceId, now)
                handleLastUpdatedRequest(it, resourceId, now)
            }
        }
        this.lastContact = now
    }

    private fun handleDeltaRequest(server: DiscordServer, resourceId: String, now: DateTime) {
        val result = McNativeHttpRequestBuilder(resourceId, deltaStart = lastContact, deltaEnd = now, details = true).createRequest()
        result.forEach { version: ResourceVersion ->
            server.guild?.let { guild ->
                if(version.status == ResourceVersionStatus.TESTING) {
                    val databaseResult = findBetaProcess(guild.idLong, resourceId, version)
                    val entry = databaseResult.firstOrNull()
                    if(version.versionInfo.qualifier == "BETA") {
                        if(entry == null) {
                            //Create
                            handleCreateBetaVersion(server, guild, version, resourceId)
                        } else {
                            val minor = entry.getInt("MinorVersion")
                            if(minor == version.versionInfo.minor) {
                                //Update
                                handleUpdateBetaVersion(guild, version, entry, resourceId)
                            }
                        }

                    }
                } else if(version.versionInfo.qualifier == "RELEASE") {
                    val databaseResult = findBetaProcess(guild.idLong, resourceId, version)
                    val entry = databaseResult.firstOrNull()
                    if(entry != null) {
                        val minor = entry.getInt("MinorVersion")
                        val patchNumber = entry.getInt("PatchNumber")
                        if(version.versionInfo.minor == minor && version.versionInfo.patch == patchNumber) {
                            //Beta was released
                            handleNewReleaseVersion(server, entry, guild, version, resourceId)
                        }
                    }
                }
            }
        }
    }

    private fun handleCreateBetaVersion(server: DiscordServer, guild: Guild, version: ResourceVersion, resourceId: String) {
        server.configuration.getCategory(guild, Categories.TESTING)
                ?.createTextChannel(getChannelName(resourceId, version))
                ?.queue { channel ->
                    channel.manager.setTopic(getChannelTopic(resourceId, version)).queue()
                    channel.sendMessage(buildTestingBetaProcessMessage(bot.resourceManager.getResourceName(resourceId), version.versionInfo.name,
                            version.description, version.testCases)).queue { message ->
                        bot.storage.discordServerBetaProcessesCollection.insert {
                            set("GuildId", guild.idLong)
                            set("State", "Testing")
                            set("ChannelId", channel.idLong)
                            set("ChannelControlMessageId", message.idLong)
                            set("ResourceId", resourceId)
                            set("MajorVersion", version.versionInfo.major)
                            set("MinorVersion", version.versionInfo.minor)
                            set("PatchNumber", version.versionInfo.patch)
                            set("BuildNumber", version.versionInfo.build)
                        }.executeAsync()
                    }
                }
    }

    private fun handleUpdateBetaVersion(guild: Guild, version: ResourceVersion, entry: QueryResultEntry, resourceId: String) {
        guild.getTextChannelById(entry.getLong("ChannelId"))?.let {
            it.manager.setName(getChannelName(resourceId, version)).queue()
            it.manager.setTopic(getChannelTopic(resourceId, version)).queue()
            it.sendMessage(buildTestingBetaProcessMessage(bot.resourceManager.getResourceName(resourceId),
                            version.versionInfo.name, version.description, version.testCases)).queue {
                bot.storage.discordServerBetaProcessesCollection.update {
                    set("ChannelControlMessageId", it.idLong)
                    set("PatchNumber", version.versionInfo.patch)
                    set("BuildNumber", version.versionInfo.build)
                    where("Id", entry.getInt("Id"))
                }.executeAsync()
            }
        }
    }

    private fun handleNewReleaseVersion(server: DiscordServer, entry: QueryResultEntry, guild: Guild, version: ResourceVersion, resourceId: String) {
        bot.storage.discordServerBetaProcessesCollection.update {
            set("State", "Passed")
            set("PassedTime", DateTime.now())
            where("Id", entry.getInt("Id"))
        }.executeAsync()
        server.configuration.getCategory(guild, Categories.PASSED)?.let { category ->
            guild.getTextChannelById(entry.getLong("ChannelId"))?.let {
                it.manager.setParent(category).queue()
                it.sendMessage(EmbedBuilder()
                        .setColor(Color(23, 143, 57))
                        .setTitle("Tests has been passed and released under version ${version.versionInfo.name}")
                        .build()).queue()
                it.manager.setTopic(getChannelTopic(resourceId, version, " (Passed)")).queue()
            }
        }
    }

    private fun findBetaProcess(guildId: Long, resourceId: String, version: ResourceVersion): QueryResult {
        return bot.storage.discordServerBetaProcessesCollection.find {
            where("GuildId", guildId)
            where("ResourceId", resourceId)
            where("MajorVersion", version.versionInfo.major)
            where("State", "Testing")
        }.execute()
    }

    private fun buildTestingBetaProcessMessage(resourceName: String, versionName: String, description: String?, testCases: String?): MessageEmbed {
        val builder = EmbedBuilder()
        builder.setTitle("__**$resourceName v$versionName**__")
        builder.setColor(Color(28, 138, 217))
        description?.let {
            builder.appendDescription(it).appendDescription("\n\n")
        }
        testCases?.let {
            builder.appendDescription(getTestCasesMessage(it))
        }
        return builder.build()
    }

    private fun handleLastUpdatedRequest(server: DiscordServer, resourceId: String, now: DateTime) {
        val result = McNativeHttpRequestBuilder(resourceId, lastUpdatedStart = lastContact, lastUpdatedEnd = now, details = true).createRequest()
        result.forEach { version ->
            server.guild?.let { guild ->
                val databaseResult = findBetaProcess(server.guildId, resourceId, version)
                val testCases = version.testCases
                val description = version.description
                val entry = databaseResult.firstOrNull()
                if(entry != null) {
                    guild.getTextChannelById(entry.getLong("ChannelId"))?.editMessageById(entry.getLong("ChannelControlMessageId"),
                            buildTestingBetaProcessMessage(bot.resourceManager.getResourceName(resourceId), version.versionInfo.name,
                                    description, testCases))?.queue()
                }
            }
        }
    }

    private fun getChannelName(resourceId: String, version: ResourceVersion): String {
        return "${bot.resourceManager.getResourceName(resourceId)}-${version.versionInfo.minor}-beta"
    }

    private fun getChannelTopic(resourceId: String, version: ResourceVersion, suffix: String = ""): String {
        return "${bot.resourceManager.getResourceName(resourceId)} v${version.versionInfo.name}$suffix"
    }

    private fun getTestCasesMessage(raw: String): String {
        println(raw)
        val data = DocumentFileType.JSON.reader.read(raw)

        if(data.isEmpty || !data.contains("cases")) return ""
        val builder = StringBuilder()
        builder.append("__**Test Cases**__")
        data.getDocument("cases").forEach {
            val document = it.toDocument()
            val title = document.getString("title")
            builder.append("\n\n:arrow_forward: **$title**")
            document.getDocument("actions").forEach { entry ->
                builder.append("\n- ${entry.toPrimitive().asString}")
            }
        }
        return builder.toString()
    }
}