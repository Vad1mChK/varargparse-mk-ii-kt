package org.vad1mchk.varargparse.mk2.database.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.vad1mchk.varargparse.mk2.entities.IsuPerson

object Members : LongIdTable() {
    val userId = long("user_id")
    val isuNumber = integer("isu_number").check { it.between(IsuPerson.MIN_ISU_NUMBER, IsuPerson.MAX_ISU_NUMBER) }
    val fullName = varchar("full_name", 255)
}