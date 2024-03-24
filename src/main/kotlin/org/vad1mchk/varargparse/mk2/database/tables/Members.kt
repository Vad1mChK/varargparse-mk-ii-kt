package org.vad1mchk.varargparse.mk2.database.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.vad1mchk.varargparse.mk2.entities.MemberLevel

object Members : LongIdTable() {
    val userId = long("user_id")
    val level = enumerationByName<MemberLevel>("level", 16)
}