package org.vad1mchk.varargparse.mk2.entities

import kotlinx.serialization.Serializable


/**
 * Enum class representing various interjections that can be used in a conversation or argument.
 *
 * @property OBJECTION Represents an objection or disagreement.
 * @property HOLD_IT Used to pause or stop a conversation temporarily.
 * @property TAKE_THAT A response to a challenge or accusation.
 * @property GOTCHA A phrase used to express that someone has been caught in a mistake or contradiction.
 * @property EUREKA A phrase used to express sudden realization or discovery.
 * @property NOT_SO_FAST A phrase used to express disagreement or challenge to someone's statement.
 * @property OVERRULED A phrase used to overrule or reject someone's statement or argument.
 */
@Serializable
enum class Interjection {
    OBJECTION,
    HOLD_IT,
    TAKE_THAT,
    GOTCHA,
    EUREKA,
    NOT_SO_FAST,
    OVERRULED,
}