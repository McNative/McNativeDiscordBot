package org.mcnative.discordbot

import java.util.*

class VersionInfo(val name: String, val major: Int, val minor: Int, val patch: Int, val build: Int, val qualifier: String) {

    override fun toString(): String {
        return name
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VersionInfo) return false
        return name.equals(other.name, ignoreCase = true)
    }

    override fun hashCode(): Int {
        return Objects.hash(name, build)
    }

    companion object {
        var UNKNOWN = VersionInfo("Unknown", 0, 0, 0, 0, "Unknown")
        fun parse(version: String): VersionInfo {

            val versionAndQualifier = version.split("-").toTypedArray()
            val parts = versionAndQualifier[0].split(".").toTypedArray()

            val major = if (parts.isNotEmpty() && isNaturalNumber(parts[0])) parts[0].toInt() else 0
            val minor = if (parts.size >= 2 && isNaturalNumber(parts[1])) parts[1].toInt() else 0
            val patch = if (parts.size >= 3 && isNaturalNumber(parts[2])) parts[2].toInt() else 0
            val build = if (parts.size >= 4 && isNaturalNumber(parts[3])) parts[3].toInt() else 0
            val qualifier = if (versionAndQualifier.size > 1) versionAndQualifier[1] else "RELEASE"
            return VersionInfo(version, major, minor, patch, build, qualifier)
        }

        private fun isNaturalNumber(value: String): Boolean {
            for (c in value.toCharArray()) if (!Character.isDigit(c)) return false
            return true
        }
    }
}