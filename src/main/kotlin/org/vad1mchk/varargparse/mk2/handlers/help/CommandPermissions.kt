package org.vad1mchk.varargparse.mk2.handlers.help

import kotlinx.serialization.Serializable

@Serializable
enum class CommandPermissions(val representation: String) {
    ALL("\uD83C\uDF10 Все пользователи"),
    ADMIN("\uD83D\uDEE1 Только админы группы"),
    OWNER("\uD83D\uDD11 Только владелец бота")
}