package org.mcnative.discordbot

import io.github.cdimascio.dotenv.Dotenv
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.pretronic.databasequery.api.driver.config.DatabaseDriverConfig
import net.pretronic.databasequery.sql.dialect.Dialect
import net.pretronic.databasequery.sql.driver.config.SQLDatabaseDriverConfigBuilder
import java.net.InetSocketAddress


class Config {

    companion object {
        val dotenv = Dotenv.configure().ignoreIfMissing().load()

        fun getenv(name: String, dotenv: Dotenv = Config.dotenv): String {
            return getenvOrNull(name, dotenv)?: throw IllegalArgumentException("Can't load environment variable $name")
        }

        fun getenvOrNull(name: String, dotenv: Dotenv = Config.dotenv): String? {
            if (System.getenv(name) != null) {
                return System.getenv(name)
            } else if (dotenv[name] != null) {
                return dotenv[name]
            }
            return null
        }
    }

    var databaseName: String = getenv("DATABASE_NAME")
    var storage: DatabaseDriverConfig<*> = SQLDatabaseDriverConfigBuilder()
            .setAddress(InetSocketAddress.createUnresolved(getenv("DATABASE_HOST"), getenv("DATABASE_PORT").toInt()))
            .setDialect(Dialect.byName(getenv("DATABASE_DIALECT")))
            .setUsername(getenv("DATABASE_USERNAME"))
            .setPassword(getenv("DATABASE_PASSWORD"))
            .build()

    var botToken : String = getenv("BOT_TOKEN")

    var botOnlineStatus = OnlineStatus.fromKey(getenv("BOT_ONLINE_STATUS"))
    var botActivityType = Activity.ActivityType.valueOf(getenv("BOT_ACTIVITY_TYPE"))
    var botActivityName = getenv("BOT_ACTIVITY_NAME")
    var botActivityUrl: String? = getenv("BOT_ACTIVITY_URL")

    var mcNativeBaseApiUrl = getenv("MCNATIVE_BASE_API_URL")

    fun init(bot: McNativeDiscordBot) : Config {

        return this
    }
}