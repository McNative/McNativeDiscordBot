package org.mcnative.discordbot

import com.jagrosh.jdautilities.command.CommandClientBuilder
import io.sentry.Sentry
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.pretronic.databasequery.api.driver.DatabaseDriverFactory
import net.pretronic.databasequery.api.driver.config.DatabaseDriverConfig
import net.pretronic.libraries.concurrent.TaskScheduler
import net.pretronic.libraries.concurrent.simple.SimpleTaskScheduler
import net.pretronic.libraries.document.Document
import net.pretronic.libraries.document.DocumentRegistry
import net.pretronic.libraries.document.type.DocumentFileType
import org.joda.time.DateTime
import org.mcnative.discordbot.requests.McNativeHttpRequestBuilder
import org.mcnative.discordbot.requests.ResourceVersion
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class McNativeDiscordBot {

    companion object {
        lateinit var INSTANCE : McNativeDiscordBot
        val logger: Logger = LoggerFactory.getLogger(McNativeDiscordBot::class.java)
    }

    val scheduler: TaskScheduler = SimpleTaskScheduler()
    val config : Config
    val storage: Storage
    val jda : JDA

    init {
        INSTANCE = this


        val sentryDsn = Config.getenvOrNull("SENTRY_DSN")
        if(sentryDsn != null) {
            Sentry.init { options -> options.dsn = sentryDsn }
        }

        logger.info("McNativeDiscordBot starting...")

        registerDocumentAdapters()
        this.config = Config().init(this)
        this.storage = initStorage()


        this.jda = initJDA()

        println(McNativeHttpRequestBuilder("0249f842-de95-42df-b611-7ad390d90086", details = true, lastUpdatedStart = DateTime.now().minusDays(30), lastUpdatedEnd = DateTime.now()).createRequest())
    }

    private fun initStorage(): Storage {
        val driver = DatabaseDriverFactory.create("DiscordBot", this.config.storage)
        driver.connect()
        return Storage(driver.getDatabase(this.config.databaseName))
    }

    private fun initJDA(): JDA {
        val commandClientBuilder = CommandClientBuilder()
                .setPrefix("!")
                .setStatus(config.botOnlineStatus)
                .setActivity(Activity.of(config.botActivityType, config.botActivityName, config.botActivityUrl))
                .setOwnerId("246659669077131264")

        val jda = JDABuilder.create(this.config.botToken, GatewayIntent.values().toList())
                .setAutoReconnect(true)
                .addEventListeners(commandClientBuilder.build(), BotListeners(this))
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build()
        jda.awaitReady()
        return jda
    }

    private fun registerDocumentAdapters() {
        DocumentRegistry.getDefaultContext().registerAdapter(ResourceVersion::class.java, ResourceVersion.Adapter())
    }
}