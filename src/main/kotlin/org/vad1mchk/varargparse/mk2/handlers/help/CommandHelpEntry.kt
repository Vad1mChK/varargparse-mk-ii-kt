package org.vad1mchk.varargparse.mk2.handlers.help

import kotlinx.serialization.Serializable

@Serializable
data class CommandHelpEntry(
    val name: String,
    val description: String,
    val permissions: CommandPermissions,
    val usage: String,
)
