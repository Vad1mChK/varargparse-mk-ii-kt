package org.vad1mchk.varargparse.mk2.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.vad1mchk.varargparse.mk2.entities.Interjection

@Serializable
data class PublicConfig(
    val interjections: Map<Interjection, InterjectionConfig>
) {
    @Serializable
    data class InterjectionConfig(
        val name: String,
        @SerialName("file_ids")
        val fileIds: Map<String, String>
    )
}
