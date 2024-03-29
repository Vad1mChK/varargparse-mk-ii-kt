package org.vad1mchk.varargparse.mk2.database.tables

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.vad1mchk.varargparse.mk2.util.now

object Quotes : LongIdTable() {
    val submitterId = long("submitter_id")
    val authorName = varchar("author_name", 255)
    val quote = text("quote")
    val creationDateTime = datetime("creation_date_time").clientDefault { LocalDateTime.now() }

    init {
        uniqueIndex("unique_author_and_quote", authorName, quote)
    }
}