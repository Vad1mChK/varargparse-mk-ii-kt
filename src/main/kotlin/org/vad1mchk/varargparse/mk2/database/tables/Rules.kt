package org.vad1mchk.varargparse.mk2.database.tables

import org.jetbrains.exposed.dao.id.LongIdTable

/**
 * Represents the "Rules" table in the database with a LongId primary key.
 *
 * This table is used to store rules associated with chat rooms. Each rule is uniquely identified by its ID,
 * and contains information such as the chat room's ID, the name of the rule, and a description of the rule.
 */
object Rules : LongIdTable(name = "vap_rules") {
    const val MAX_RULES_COUNT_PER_CHAT = 15

    // Represents the ID of the chat room. Defaults to 0 if not specified.
    val chatId = long("chat_id").default(0L)

    // The name of the rule. Stored as a variable character string with a maximum length of 255 characters.
    val name = varchar("name", 255).uniqueIndex()

    // A textual description of what the rule entails.
    val description = text("description")
}