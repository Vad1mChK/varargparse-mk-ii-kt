package org.vad1mchk.varargparse.mk2.handlers

import com.github.kotlintelegrambot.dispatcher.handlers.HandleMessage
import kotlinx.datetime.LocalDateTime
import org.vad1mchk.varargparse.mk2.config.Config
import org.vad1mchk.varargparse.mk2.database.addEvent
import org.vad1mchk.varargparse.mk2.entities.EventType
import org.vad1mchk.varargparse.mk2.util.fromUnixTimestamp

val statsMessageHandler: HandleMessage = {
    addEvent(
        Config.database,
        LocalDateTime.fromUnixTimestamp(message.date),
        message.chat.id,
        message.from?.id,
        null,
        EventType.MESSAGE
    )
}

val statsJoinGroupHandler: HandleMessage = {
    addEvent(
        Config.database,
        LocalDateTime.fromUnixTimestamp(message.date),
        message.chat.id,
        null,
        message.newChatMembers?.size,
        EventType.JOIN_GROUP
    )
}

val statsLeaveGroupHandler: HandleMessage = {
    addEvent(
        Config.database,
        LocalDateTime.fromUnixTimestamp(message.date),
        message.chat.id,
        null,
        null,
        EventType.LEAVE_GROUP
    )
}