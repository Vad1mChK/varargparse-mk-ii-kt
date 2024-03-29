package org.vad1mchk.varargparse.mk2.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.vad1mchk.varargparse.mk2.config.Config
import org.vad1mchk.varargparse.mk2.database.tables.Members
import org.vad1mchk.varargparse.mk2.database.tables.Quotes

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

    SchemaUtils.create(Quotes, Members)
}