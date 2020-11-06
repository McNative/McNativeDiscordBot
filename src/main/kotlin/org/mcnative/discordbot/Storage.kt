package org.mcnative.discordbot

import net.pretronic.databasequery.api.Database
import net.pretronic.databasequery.api.collection.DatabaseCollection

class Storage(database: Database) {

    val discordServersCollection: DatabaseCollection = database.getCollection("discord_servers")

    val discordServerBetaProcessesCollection: DatabaseCollection = database.getCollection("discord_server_beta_processes")
}