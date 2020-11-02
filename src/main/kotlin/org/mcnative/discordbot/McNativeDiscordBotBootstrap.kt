package org.mcnative.discordbot

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

fun main() {
    McNativeDiscordBot()
    println(DateTime.now().toString(DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")))//2020-11-01T18:16:24.852Z
}