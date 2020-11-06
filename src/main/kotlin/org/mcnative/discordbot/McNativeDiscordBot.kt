package org.mcnative.discordbot

import com.jagrosh.jdautilities.command.CommandClientBuilder
import io.sentry.Sentry
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.pretronic.databasequery.api.driver.DatabaseDriverFactory
import net.pretronic.libraries.concurrent.Task
import net.pretronic.libraries.concurrent.TaskScheduler
import net.pretronic.libraries.concurrent.simple.SimpleTaskScheduler
import net.pretronic.libraries.document.DocumentRegistry
import net.pretronic.libraries.utility.interfaces.ObjectOwner
import org.mcnative.discordbot.commands.setup.DiscordServerSetupCommand
import org.mcnative.discordbot.discordserver.DiscordServerManager
import org.mcnative.discordbot.requests.ResourceVersion
import org.mcnative.discordbot.resource.ResourceManager
import org.mcnative.discordbot.tasks.ArchivedTask
import org.mcnative.discordbot.tasks.BetaProcessTask
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class McNativeDiscordBot {

    companion object {
        lateinit var INSTANCE : McNativeDiscordBot
        val logger: Logger = LoggerFactory.getLogger(McNativeDiscordBot::class.java)
    }

    val scheduler: TaskScheduler = SimpleTaskScheduler()
    val config : Config
    val storage: Storage
    val jda: JDA
    val serverManager: DiscordServerManager
    val resourceManager: ResourceManager
    val tasks: MutableCollection<Task> = ArrayList()

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
        this.serverManager = DiscordServerManager(this)
        this.resourceManager = ResourceManager()

        this.jda = initJDA()
        startTasks()
    }

    fun onShutdown() {
        logger.info("McNativeDiscordBot shutting down...")
        this.tasks.forEach { it.stop() }
        this.jda.shutdown()
        logger.info("McNativeDiscordBot successfully shutdown")
    }

    private fun initStorage(): Storage {
        val driver = DatabaseDriverFactory.create("McNativeDiscordBot", this.config.storage)
        driver.connect()
        return Storage(driver.getDatabase(this.config.databaseName))
    }

    private fun initJDA(): JDA {
        val commandClientBuilder = CommandClientBuilder()
                .setPrefix("!")
                .setStatus(config.botOnlineStatus)
                .setActivity(Activity.of(config.botActivityType, config.botActivityName, config.botActivityUrl))
                .setOwnerId("246659669077131264")
                .useHelpBuilder(true)
                .addCommands(DiscordServerSetupCommand(this))

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

    private fun startTasks() {
        this.tasks.add(this.scheduler.createTask(ObjectOwner.SYSTEM)
                .interval(1, TimeUnit.MINUTES)
                .async()
                .execute(BetaProcessTask(this)))
        this.tasks.add(this.scheduler.createTask(ObjectOwner.SYSTEM)
                .interval(1, TimeUnit.HOURS)
                .async()
                .execute(ArchivedTask(this)))
    }
}