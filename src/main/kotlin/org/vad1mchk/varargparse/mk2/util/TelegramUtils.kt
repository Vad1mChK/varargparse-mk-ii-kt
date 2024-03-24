package org.vad1mchk.varargparse.mk2.util

import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.User

fun Message.chatId() = ChatId.fromId(this.senderChat?.id ?: this.chat.id)

val User.fullName get() = (this.firstName + lastName?.let { " $it" }.orEmpty())

fun User.mentionMarkdown(firstNameOnly: Boolean = false) =
    "[${if (firstNameOnly) this.firstName else this.fullName}](tg://user?id=${this.id})"