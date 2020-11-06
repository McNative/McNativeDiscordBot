package org.mcnative.discordbot.discordserver

import net.dv8tion.jda.api.entities.Guild
import net.pretronic.databasequery.api.dsl.find
import net.pretronic.databasequery.api.dsl.insert
import net.pretronic.libraries.caching.ArrayCache
import net.pretronic.libraries.caching.Cache
import net.pretronic.libraries.caching.CacheQuery
import net.pretronic.libraries.document.type.DocumentFileType
import net.pretronic.libraries.utility.Validate
import net.pretronic.libraries.utility.list.ArrayCallbackList
import net.pretronic.libraries.utility.list.CallbackCollection
import net.pretronic.libraries.utility.map.callback.CallbackMap
import net.pretronic.libraries.utility.map.callback.ConcurrentCallbackMap
import net.pretronic.libraries.utility.reflect.TypeReference
import org.mcnative.discordbot.McNativeDiscordBot
import java.lang.Exception
import java.util.concurrent.CompletableFuture

class DiscordServerManager(private val bot: McNativeDiscordBot) {

    val servers: Cache<DiscordServer> = ArrayCache()

    init {
        servers.registerQuery("byGuildId", object : CacheQuery<DiscordServer>{

            override fun check(server: DiscordServer, identifiers: Array<out Any>): Boolean {
                return server.guildId == identifiers[0] as Long
            }

            override fun validate(identifiers: Array<out Any>) {
                Validate.isTrue(identifiers.size == 1 && identifiers[0] is Long)
            }

            override fun load(identifiers: Array<out Any>): DiscordServer? {
                try {
                    val guildId = identifiers[0] as Long
                    val result = bot.storage.discordServersCollection.find { where("GuildId", guildId) }.execute()
                    if(result.isEmpty) {
                        bot.storage.discordServersCollection.insert {
                            set("GuildId", guildId)
                        }.executeAsync()
                        return DiscordServer(guildId)
                    }
                    val entry = result.first()
                    val rawCategoryIds = DocumentFileType.JSON.reader.read(entry.getString("CategoryIds"))
                    val categoryIds: CallbackMap<String, Long> = if(rawCategoryIds == null || rawCategoryIds.isEmpty) ConcurrentCallbackMap() else {
                        rawCategoryIds.getAsObject(object : TypeReference<ConcurrentCallbackMap<String, Long>>() {})
                    }

                    val rawBetaProcessResourceIds = DocumentFileType.JSON.reader.read(entry.getString("BetaProcessResourceIds"))
                    val betaProcessResourceIds: CallbackCollection<String> = if(rawBetaProcessResourceIds == null || rawBetaProcessResourceIds.isEmpty) ArrayCallbackList() else {
                        rawBetaProcessResourceIds.getAsObject(object : TypeReference<ArrayCallbackList<String>>() {})
                    }
                    return DiscordServer(guildId, DiscordServerConfiguration(entry.getLong("TesterRoleId"), categoryIds, betaProcessResourceIds))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return null
            }
        })
        loadAll()
    }
    //Temporary
    private fun loadAll() {
        bot.storage.discordServersCollection.find().execute().forEach {
            getServer(it.getLong("GuildId"))
        }
    }

    fun getServer(guildId: Long): CompletableFuture<DiscordServer> {
        return servers.getAsync("byGuildId", guildId)
    }

    fun getServer(guild: Guild): CompletableFuture<DiscordServer> {
        return getServer(guild.idLong)
    }
}