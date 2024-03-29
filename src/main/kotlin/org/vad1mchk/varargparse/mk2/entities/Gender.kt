package org.vad1mchk.varargparse.mk2.entities

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
enum class Gender {
    @SerialName("male") MALE,
    @SerialName("female") FEMALE,
    @SerialName("other") OTHER
}