package org.mcnative.discordbot.requests

import net.pretronic.libraries.document.Document
import net.pretronic.libraries.document.adapter.DocumentAdapter
import net.pretronic.libraries.document.entry.DocumentBase
import net.pretronic.libraries.document.entry.DocumentEntry
import net.pretronic.libraries.utility.reflect.TypeReference
import org.joda.time.DateTime
import org.mcnative.discordbot.VersionInfo
import java.util.*

class ResourceVersion(val id: Int, val versionInfo: VersionInfo, val time: DateTime, val status: ResourceVersionStatus, val description: String?, val changes: String?, val testCases: String?) {

    companion object {

        fun parse(id: Int, name: String, time: String, status: String, description: String?
                  , changes: String?, testCases: String?): ResourceVersion {
            val versionInfo = VersionInfo.parse(name)
            val time = DateTime(time)
            val status = ResourceVersionStatus.valueOf(status)
            return ResourceVersion(id, versionInfo, time, status, description, changes, testCases)
        }
    }

    class Adapter: DocumentAdapter<ResourceVersion> {

        override fun read(base: DocumentBase, reference: TypeReference<ResourceVersion>): ResourceVersion {
            val data = base.toDocument()
            return parse(data.getInt("Id"), data.getString("Name"), data.getString("Time"), data.getString("Status")
                    , data.getString("Description"), data.getString("Changes"), data.getString("TestCases"))
        }

        override fun write(p0: String?, p1: ResourceVersion?): DocumentEntry {
            TODO("Not yet implemented")
        }

    }
}