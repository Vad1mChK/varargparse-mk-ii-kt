package org.vad1mchk.varargparse.mk2.database.entities

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.vad1mchk.varargparse.mk2.database.tables.Quotes

class Quote(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Quote>(Quotes)

    var submitterId by Quotes.submitterId
    var authorName by Quotes.authorName
    var text by Quotes.quote
    var creationDateTime by Quotes.creationDateTime
}