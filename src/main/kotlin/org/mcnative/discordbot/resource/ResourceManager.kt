package org.mcnative.discordbot.resource

import net.pretronic.libraries.document.type.DocumentFileType
import net.pretronic.libraries.utility.http.HttpClient
import org.mcnative.discordbot.Config

class ResourceManager {

    val resources: MutableCollection<Resource> = ArrayList()

    fun getResource(id: String): Resource? {
        var resource = this.resources.firstOrNull { it.id == id }
        if(resource == null) {
            val httpClient = HttpClient()
            httpClient.setUrl("${Config.getenv("MCNATIVE_BASE_API_URL")}/resources/$id")
            val result = httpClient.connect()
            if(result.code == 200) {
                val data = DocumentFileType.JSON.reader.read(result.content)
                resource = Resource(id, data.getString("Name"))
                resources.add(resource)
            }
        }
        return resource
    }

    fun getResourceNameOrNull(id: String): String? {
        return getResource(id)?.name
    }

    fun getResourceName(id: String): String {
        return getResourceNameOrNull(id)?:"not-available"
    }
}