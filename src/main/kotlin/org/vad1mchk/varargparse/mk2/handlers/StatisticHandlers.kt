package org.vad1mchk.varargparse.mk2.handlers

import com.github.kotlintelegrambot.dispatcher.handlers.HandleMessage
import kotlinx.datetime.LocalDateTime
import org.vad1mchk.varargparse.mk2.config.Config
import org.vad1mchk.varargparse.mk2.database.addEvent
import org.vad1mchk.varargparse.mk2.database.addEventWithTruncation
import org.vad1mchk.varargparse.mk2.database.isHistoryEnabledForChat
import org.vad1mchk.varargparse.mk2.entities.EventType
import org.vad1mchk.varargparse.mk2.util.fromUnixTimestamp

val statsMessageHandler: HandleMessage = {
    if (Config.database.isHistoryEnabledForChat(message.chat.id)) {
        Config.database.addEventWithTruncation(
            LocalDateTime.fromUnixTimestamp(message.date),
            message.chat.id,
            message.from?.id,
            null,
            EventType.MESSAGE
        )
    }
}

val statsJoinGroupHandler: HandleMessage = {
    if (Config.database.isHistoryEnabledForChat(message.chat.id)) {
        Config.database.addEventWithTruncation(
            LocalDateTime.fromUnixTimestamp(message.date),
            message.chat.id,
            null,
            message.newChatMembers?.size,
            EventType.JOIN_GROUP
        )
    }
}

val statsLeaveGroupHandler: HandleMessage = {
    if (Config.database.isHistoryEnabledForChat(message.chat.id)) {
        Config.database.addEventWithTruncation(
            LocalDateTime.fromUnixTimestamp(message.date),
            message.chat.id,
            null,
            null,
            EventType.LEAVE_GROUP
        )
    }
}