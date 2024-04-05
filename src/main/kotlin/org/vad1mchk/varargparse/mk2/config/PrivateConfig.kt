package org.vad1mchk.varargparse.mk2.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.vad1mchk.varargparse.mk2.database.DatabaseCredentials

@Serializable
data class PrivateConfig(
    val token: String,
    @SerialName("owner_id")
    val ownerId: Long,
    val database: DatabaseConfig
) {
    @Serializable
    data class DatabaseConfig(
        val url: String,
        val credentials: DatabaseCredentials
    )
}