package org.vad1mchk.varargparse.mk2.handlers.help

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString

object CommandHelpHolder {
    val commands: Map<String, CommandHelpEntry> by lazy { loadFromYaml("/help.yaml") }

    private fun loadFromYaml(path: String): Map<String, CommandHelpEntry> {
        val yamlString: String = javaClass.getResourceAsStream(path)?.bufferedReader()?.use { reader ->
            reader.readText()
        } ?: throw RuntimeException("Couldn't find or read the help file.")

        return Yaml.default.decodeFromString<Map<String, CommandHelpEntry>>(yamlString)
    }

    operator fun get(name: String): CommandHelpEntry? = commands[name]
}