package org.vad1mchk.varargparse.mk2.database.entities

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.vad1mchk.varargparse.mk2.database.tables.Rules

class Rule(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Rule>(Rules)

    var chatId by Rules.chatId

    var name by Rules.name

    var description by Rules.description
}