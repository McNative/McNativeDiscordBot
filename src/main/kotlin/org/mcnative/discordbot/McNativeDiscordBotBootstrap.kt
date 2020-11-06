package org.mcnative.discordbot

fun main() {
    val bot = McNativeDiscordBot()
    Runtime.getRuntime().addShutdownHook(Thread {
        bot.onShutdown()
    })
}