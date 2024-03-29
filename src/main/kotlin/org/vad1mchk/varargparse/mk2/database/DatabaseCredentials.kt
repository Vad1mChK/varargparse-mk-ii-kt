package org.vad1mchk.varargparse.mk2.database

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface DatabaseCredentials {
    @Serializable
    @SerialName("username_password")
    data class UserPasswordDatabaseCredentials(val username: String, val password: String) : DatabaseCredentials

    @Serializable
    @SerialName("pgpass")
    data class PgpassDatabaseCredentials(val username: String) : DatabaseCredentials
}