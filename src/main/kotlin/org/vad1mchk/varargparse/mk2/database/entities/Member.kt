package org.vad1mchk.varargparse.mk2.database.entities

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.vad1mchk.varargparse.mk2.database.tables.Members

/**
 * Represents a member of the community, which can have a certain level of access to certain features.
 *
 * @param id the unique identifier of the member
 * @param userId the unique identifier of the user that corresponds to this member
 * @param level the level of access that the member has, which determines what features they can access
 */
class Member(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Member>(Members)

    /**
     * The unique identifier of the user that corresponds to this member.
     */
    var userId by Members.userId

    /**
     * The level of access that the member has, which determines what features they can access.
     */
    var level by Members.level
}