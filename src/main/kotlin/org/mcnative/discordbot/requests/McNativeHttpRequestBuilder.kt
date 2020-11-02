package org.mcnative.discordbot.requests

import net.pretronic.libraries.document.Document
import net.pretronic.libraries.document.type.DocumentFileType
import net.pretronic.libraries.utility.http.HttpClient
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.mcnative.discordbot.McNativeDiscordBot
import java.io.BufferedReader
import java.io.InputStreamReader


class McNativeHttpRequestBuilder(val resourceId: String,
                                 val qualifier: String? = null,
                                 val page: Int? = null,
                                 val pageSize: Int? = null,
                                 val details: Boolean? = null,
                                 val deltaStart: DateTime? = null,
                                 val deltaEnd: DateTime? = null,
                                 val lastUpdatedStart: DateTime? = null,
                                 val lastUpdatedEnd: DateTime? = null) {

    private val httpClient = HttpClient()

    init {
        if((deltaStart != null || deltaEnd != null) && (lastUpdatedStart!= null || lastUpdatedEnd != null)) {
            throw IllegalArgumentException("Can't create http request. Only delta or lastUpdated is supported")
        }
        httpClient.setRequestMethod("GET")

        val bot = McNativeDiscordBot.INSTANCE
        val urlBuilder = StringBuilder().append("${bot.config.mcNativeBaseApiUrl}/resources/$resourceId/versions/")
        val properties = HashMap<String, String>()
        if(deltaStart != null && deltaEnd != null) {
            urlBuilder.append("delta")
            properties["start"] = deltaStart.toDateTimeISO().toString(DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
            properties["end"] = deltaEnd.toString(DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
        } else if(lastUpdatedStart != null && lastUpdatedEnd != null) {
            urlBuilder.append("lastupdated")
            properties["start"] = lastUpdatedStart.toString(DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
            properties["end"] = lastUpdatedEnd.toString(DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
        } else {
            if(qualifier != null) httpClient.addProperty("qualifier", qualifier)
            if(page != null) httpClient.addProperty("page", page)
            if(pageSize != null) httpClient.addProperty("pageSize", pageSize)
        }
        if(details != null) {
            properties["details"] = details.toString()
        }
        var first = true
        properties.forEach {
            if(first) {
                first = false
                urlBuilder.append("?")
            } else {
                urlBuilder.append("&")
            }
            urlBuilder.append(it.key).append("=").append(it.value)
        }
        println(urlBuilder.toString())
        httpClient.setUrl(urlBuilder.toString())
    }

    fun createRequest(): List<ResourceVersion> {
        val result = httpClient.connect()
        val builder = StringBuilder()
        val reader = BufferedReader(InputStreamReader(result.inputStream))

        var line = reader.readLine()
        do {
            builder.append(line)
            line = reader.readLine()
        } while (line != null)

        val rawData = builder.toString()
        println(rawData)

        val document = DocumentFileType.JSON.reader.read(rawData)
        val versions = ArrayList<ResourceVersion>()
        if(document.isArray) {
            addVersions(document, versions)
        } else {
            addVersions(document.getDocument("rows"), versions)
        }
        return versions
    }

    private fun addVersions(document: Document, versions: MutableList<ResourceVersion>) {
        document.forEach {
            val data = it.toDocument()
            val version = ResourceVersion.parse(data.getInt("Id"),
                    data.getString("Name"),
                    data.getString("Time"),
                    data.getString("Status"),
                    data.getString("Description"),
                    data.getString("Changes"),
                    data.getString("TestCases"))
            versions.add(version)
        }
    }

}