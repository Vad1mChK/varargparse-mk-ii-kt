package org.vad1mchk.varargparse.mk2.database.tables

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.vad1mchk.varargparse.mk2.entities.EventType
import org.vad1mchk.varargparse.mk2.util.now

object Events : LongIdTable() {
    val createdAt = datetime("timestamp").clientDefault { LocalDateTime.now() }
    val chatId = long("chat_id")
    val userId = long("user_id").nullable()
    val joinCount = integer("join_count").nullable()
    val eventType = enumerationByName<EventType>("event_type", 16)
}