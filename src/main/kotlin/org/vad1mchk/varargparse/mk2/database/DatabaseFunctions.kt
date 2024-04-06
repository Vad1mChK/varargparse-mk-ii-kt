package org.vad1mchk.varargparse.mk2.database

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.transactions.transaction
import org.vad1mchk.varargparse.mk2.config.Config
import org.vad1mchk.varargparse.mk2.database.entities.Event
import org.vad1mchk.varargparse.mk2.database.entities.Rule
import org.vad1mchk.varargparse.mk2.database.tables.Events
import org.vad1mchk.varargparse.mk2.database.tables.Rules
import org.vad1mchk.varargparse.mk2.entities.EventType
import org.vad1mchk.varargparse.mk2.util.minus
import org.vad1mchk.varargparse.mk2.util.now
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

const val EVENTS_MAX_COUNT = 16_384

fun connectToDatabase() = when (val creds = Config.privateConfig.database.credentials) {
    is DatabaseCredentials.UserPasswordDatabaseCredentials -> {
        Database.connect(
            url = Config.privateConfig.database.url,
            user = creds.username,
            password = creds.password
        )
    }

    is DatabaseCredentials.PgpassDatabaseCredentials -> {
        Database.connect(
            url = Config.privateConfig.database.url,
            user = creds.username
        )
    }
}

fun initializeDatabase(database: Database) = transaction(database) {
    addLogger(StdOutSqlLogger)

    SchemaUtils.create(Rules, Events)

    Config.database = database
}

fun getRuleById(database: Database, ruleId: Long): Rule? = transaction(database) {
    Rule.findById(ruleId)
}

fun getRules(database: Database, chatId: Long, limit: Int? = null) = transaction(database) {
    Rule.find { Rules.chatId eq chatId }
        .let { if (limit != null) it.limit(limit) else it }
        .toList()
}

fun addRule(database: Database, chatId: Long, name: String, description: String) = transaction(database) {
    Rule.new {
        this.name = name
        this.description = description
        this.chatId = chatId
    }
}

fun getEventsByChatId(
    database: Database,
    chatId: Long,
    limit: Int = EVENTS_MAX_COUNT,
    duration: Duration = 1.days
) = transaction(database) {
    Event
        .find {
            Events.chatId eq chatId and Events.createdAt.between(
                LocalDateTime.now() - duration, LocalDateTime.now()
            )
        }
        .limit(limit)
        .toList()
}

fun addEvent(
    database: Database,
    createdAt: LocalDateTime,
    chatId: Long,
    userId: Long?,
    joinCount: Int? = null,
    eventType: EventType = EventType.MESSAGE
) = transaction(database) {
    Event.new {
        this.createdAt = createdAt
        this.chatId = chatId
        this.userId = userId
        this.joinCount = joinCount
        this.eventType = eventType
    }
}

fun getMessageCountByChatId(
    database: Database,
    chatId: Long
) = transaction(database) {
    Event.find {
        (Events.eventType eq EventType.MESSAGE) and
        (Events.chatId eq chatId)
    }.count()
}

fun getJoinCountByChatId(
    database: Database,
    chatId: Long
) = transaction(database) {
    val joinCount = Events.select(Events.joinCount.sum())
        .where {
            (Events.eventType eq EventType.JOIN_GROUP) and
            (Events.chatId eq chatId)
        }
        .singleOrNull()
        ?.get(Events.joinCount.sum()) ?: 0
    joinCount
}

fun getLeaveCountByChatId(
    database: Database,
    chatId: Long
) = transaction(database) {
    val leaveCount = Events.select(Events.id.count())
        .where {
            (Events.eventType eq EventType.LEAVE_GROUP) and
            (Events.chatId eq chatId)
        }
        .singleOrNull()
        ?.get(Events.id.count()) ?: 0
    leaveCount
}

fun getTopUserIdsByChatId(
    database: Database,
    chatId: Long,
    limit: Int = EVENTS_MAX_COUNT,
) = transaction(database) {
    Events
        .select(Events.userId, Events.id.count())
        .where {
            (Events.chatId eq chatId) and
            Events.userId.isNotNull() and
            (Events.eventType eq EventType.MESSAGE)
        }
        .groupBy(Events.userId)
        .orderBy(Events.id.count() to SortOrder.DESC)
        .limit(limit)
        .map { it[Events.userId] to it[Events.id.count()] }
        // Help complete
}

fun deleteExcessRows(
    database: Database,
    keepCount: Int = EVENTS_MAX_COUNT,
    duration: Duration = 1.days
) = transaction(database) {
    // Count the total number of rows in the table
    val rowCount = Events.selectAll().count()

    if (rowCount > keepCount) {
        // Find the ID of the last row to keep (the 16,384th row from the end based on timestamp)
        val cutoffId = Events
            .select(Events.id)
            .orderBy(Events.createdAt to SortOrder.DESC)
            .limit(keepCount)
            .lastOrNull()

        // Delete rows with IDs less than or equal to the cutoff ID
        cutoffId?.let {
            Events.deleteWhere { Events.id less cutoffId[Events.id] }
        }
    }

    Events.deleteWhere { Events.createdAt less (LocalDateTime.now() - duration) }
}