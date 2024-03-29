package org.vad1mchk.varargparse.mk2.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IsuPerson(
    @SerialName("isu") val isuNumber: Int,
    @SerialName("fio") val fullName: String,
    val gender: Gender, // JSON can also contain other fields we'll ignore
) {
    companion object {
        const val MIN_ISU_NUMBER = 100000
        const val MAX_ISU_NUMBER = 999999
    }

    init {
        require(isuNumber in MIN_ISU_NUMBER..MAX_ISU_NUMBER) {
            "Invalid ISU number: $isuNumber, must be between $MIN_ISU_NUMBER and $MAX_ISU_NUMBER"
        }
    }
}
