package org.vad1mchk.varargparse.mk2.database.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object Rules : LongIdTable() {
    val chatId = long("chat_id").default(0L)
    val name = varchar("name", 255)
    val description = text("description")

    init {
        uniqueIndex("uniqueAll", chatId, name)
    }
}