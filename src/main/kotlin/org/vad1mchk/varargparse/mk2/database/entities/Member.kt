package org.vad1mchk.varargparse.mk2.database.entities

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.vad1mchk.varargparse.mk2.database.tables.Members

class Member(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Member>(Members)

    var userId by Members.userId

    var isuNumber by Members.isuNumber

    val fullName by Members.fullName
}