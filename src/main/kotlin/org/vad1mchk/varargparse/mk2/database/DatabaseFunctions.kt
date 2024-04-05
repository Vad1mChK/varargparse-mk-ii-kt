package org.vad1mchk.varargparse.mk2.database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.vad1mchk.varargparse.mk2.config.Config
import org.vad1mchk.varargparse.mk2.database.entities.Rule
import org.vad1mchk.varargparse.mk2.database.tables.Rules

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

    SchemaUtils.create(Rules)

    Config.database = database
}

fun getRuleById(database: Database, ruleId: Long): Rule? = transaction(database) {
    Rule.findById(ruleId)
}

fun getRuleCount(database: Database) = transaction(database) {
    Rules.selectAll().count()
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