package org.vad1mchk.varargparse.mk2.entities

/**
 * Enumeration representing different types of admin privileges in the club.
 */
enum class MemberLevel{
    /**
     * Represents the bot's owner, who owns ultimate permissions
     */
    OWNER,

    /**
     * Represents one of the club's creators, who can assign or demote admins.
     */
    CREATOR,

    /**
     * Represents an ordinary club admin.
     */
    ADMIN,

    /**
     * Represents a regular club member
     */
    REGULAR;
}