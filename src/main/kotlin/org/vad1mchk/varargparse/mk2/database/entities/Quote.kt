package org.vad1mchk.varargparse.mk2.database.entities

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.vad1mchk.varargparse.mk2.database.tables.Quotes

/**
 * Represents a quote in the database.
 *
 * @param id the unique identifier of the quote
 * @param submitterId the ID of the user who submitted the quote
 * @param authorName the name of the author of the quote
 * @param text the text of the quote
 * @param creationDateTime the date and time at which the quote was created
 */
class Quote(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Quote>(Quotes)

    /**
     * The ID of the user who submitted the quote.
     */
    var submitterId by Quotes.submitterId

    /**
     * The name of the author of the quote.
     */
    var authorName by Quotes.authorName

    /**
     * The text of the quote.
     */
    var text by Quotes.quote

    /**
     * The date and time at which the quote was created.
     */
    var creationDateTime by Quotes.creationDateTime
}