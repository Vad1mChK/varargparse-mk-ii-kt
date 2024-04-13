package org.vad1mchk.varargparse.mk2.handlers.help

import kotlinx.serialization.Serializable

@Serializable
enum class CommandPermissions {
    ALL,
    ADMIN,
    OWNER
}