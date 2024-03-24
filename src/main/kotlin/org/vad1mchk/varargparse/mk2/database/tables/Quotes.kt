package org.vad1mchk.varargparse.mk2.database.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object Quotes : LongIdTable() {
    val submitterId = long("submitter_id")
    val authorName = varchar("author_name", 255)
    val quote = text("quote")
    val creationDateTime = datetime("creation_date_time")

    init {
        uniqueIndex("unique_author_and_quote", authorName, quote)
    }
}