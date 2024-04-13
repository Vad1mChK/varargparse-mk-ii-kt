package org.vad1mchk.varargparse.mk2.database

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.vad1mchk.varargparse.mk2.config.Config
import org.vad1mchk.varargparse.mk2.database.entities.Event
import org.vad1mchk.varargparse.mk2.database.entities.Rule
import org.vad1mchk.varargparse.mk2.database.tables.ChatSettings
import org.vad1mchk.varargparse.mk2.database.tables.Events
import org.vad1mchk.varargparse.mk2.database.tables.Rules
import org.vad1mchk.varargparse.mk2.entities.EventType
import org.vad1mchk.varargparse.mk2.exceptions.MaxRuleCountExceededException

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

fun Database.initialize() = transaction(this) {
    addLogger(StdOutSqlLogger)

    SchemaUtils.create(Rules, Events, ChatSettings)

    Config.database = this@initialize
}

fun Database.getRuleById(ruleId: Long): Rule? = transaction(this) {
    Rule.findById(ruleId)
}

fun Database.getRules(chatId: Long, limit: Int? = null) = transaction(this) {
    Rule.find { Rules.chatId eq chatId }
        .let { if (limit != null) it.limit(limit) else it }
        .toList()
}

fun Database.addRule(chatId: Long, name: String, description: String) = transaction(this) {
    Rule.new {
        this.name = name
        this.description = description
        this.chatId = chatId
    }
}

fun Database.addRuleIfNotExceedsLimit(
    chatId: Long,
    name: String,
    description: String
) = transaction(this) {
    val currentCount = Rule.count(Rules.chatId eq chatId)

    if (currentCount < Rules.MAX_RULES_COUNT_PER_CHAT) {
        Rule.new {
            this.name = name
            this.description = description
            this.chatId = chatId
        }
    } else {
        throw MaxRuleCountExceededException(
            "Cannot add more rules: max limit of rules (${Rules.MAX_RULES_COUNT_PER_CHAT}) exceeded."
        )
    }
}

fun Database.addEvent(
    createdAt: LocalDateTime,
    chatId: Long,
    userId: Long?,
    joinCount: Int? = null,
    eventType: EventType = EventType.MESSAGE
) = transaction(this) {
    Event.new {
        this.createdAt = createdAt
        this.chatId = chatId
        this.userId = userId
        this.joinCount = joinCount
        this.eventType = eventType
    }
}

fun Database.addEventWithTruncation(
    createdAt: LocalDateTime,
    chatId: Long,
    userId: Long?,
    joinCount: Int? = null,
    eventType: EventType = EventType.MESSAGE
) = transaction(this) {
    while (Events.selectAll().count() >= Events.MAX_EVENTS_COUNT) {
        val oldestEvent = Events.selectAll()
            .orderBy(Events.createdAt to SortOrder.ASC)
            .limit(1)
            .firstOrNull()

        if (oldestEvent != null) {
            Events.deleteWhere { Events.id eq oldestEvent[Events.id] }
        }
    }

    this@addEventWithTruncation.addEvent(createdAt, chatId, userId, joinCount, eventType)
}

fun Database.getMessageCountByChatId(
    chatId: Long
) = transaction(this) {
    Event.find {
        (Events.eventType eq EventType.MESSAGE) and
                (Events.chatId eq chatId)
    }.count()
}

fun Database.getJoinCountByChatId(
    chatId: Long
) = transaction(this) {
    val joinCount = Events.select(Events.joinCount.sum())
        .where {
            (Events.eventType eq EventType.JOIN_GROUP) and
                    (Events.chatId eq chatId)
        }
        .singleOrNull()
        ?.get(Events.joinCount.sum()) ?: 0
    joinCount
}

fun Database.getLeaveCountByChatId(
    chatId: Long
) = transaction(this) {
    val leaveCount = Events.select(Events.id.count())
        .where {
            (Events.eventType eq EventType.LEAVE_GROUP) and
                    (Events.chatId eq chatId)
        }
        .singleOrNull()
        ?.get(Events.id.count()) ?: 0
    leaveCount
}

fun Database.getTopUserIdsByChatId(
    chatId: Long,
    limit: Int = EVENTS_MAX_COUNT,
) = transaction(this) {
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

fun Database.deleteEventsByUserIdAndChatId(userId: Long, chatId: Long) = transaction(this) {
    Events.deleteWhere {
        (Events.userId eq userId) and
                (Events.chatId eq chatId)
    }
}

fun Database.deleteEventsByChatId(chatId: Long) = transaction(this) {
    Events.deleteWhere {
        (Events.chatId eq chatId)
    }
}

fun Database.isHistoryEnabledForChat(chatId: Long) = transaction(this) {
    ChatSettings
        .selectAll()
        .where { (ChatSettings.chatId eq chatId) and (ChatSettings.historyEnabled) }
        .any()
}

fun Database.setHistoryEnabledForChat(chatId: Long, enabled: Boolean) = transaction(this) {
    ChatSettings.upsert(where = { ChatSettings.chatId eq chatId }) {
        it[ChatSettings.chatId] = chatId
        it[historyEnabled] = enabled
    }
}