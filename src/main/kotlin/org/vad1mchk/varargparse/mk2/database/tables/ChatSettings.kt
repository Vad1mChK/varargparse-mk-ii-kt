package org.vad1mchk.varargparse.mk2.database.tables

import org.jetbrains.exposed.sql.Table

object ChatSettings: Table("chat_settings") {
    val chatId = long("chat_id").autoIncrement()
    val historyEnabled = bool("history_enabled").default(false)

    override val primaryKey = PrimaryKey(chatId)
}