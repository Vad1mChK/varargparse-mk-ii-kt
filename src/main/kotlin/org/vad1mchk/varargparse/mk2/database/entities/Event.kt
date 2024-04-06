package org.vad1mchk.varargparse.mk2.database.entities

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.vad1mchk.varargparse.mk2.database.tables.Events

class Event(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Event>(Events)

    var createdAt by Events.createdAt

    var chatId by Events.chatId

    var userId by Events.userId

    var joinCount by Events.joinCount

    var eventType by Events.eventType
}