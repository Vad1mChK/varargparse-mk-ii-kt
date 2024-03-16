package org.vad1mchk.varargparse.mk2

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.logging.LogLevel

fun main(args: Array<String>) {
    val bot = bot {
        token = System.getenv("vap_TOKEN")

        logLevel = LogLevel.All()

        dispatch {
            command("start") {
                bot.sendMessage(
                    chatId = message.chatId(),
                    text = "Я живой!",
                    allowSendingWithoutReply = true,
                    replyToMessageId = message.messageId,
                )
            }

            command("help") {
                bot.sendMessage(
                    chatId = message.chatId(),
                    text = bot.getMyCommands().get().map {
                        "/${it.command}: ${it.description}"
                    }.joinToString("\n"),
                    replyToMessageId = message.messageId,
                    allowSendingWithoutReply = true
                )
            }

            for (interjection in Interjection.entries) {
                command(interjection.name.lowercase()) {
                    bot.sendSticker(
                        chatId = message.chatId(),
                        sticker = interjection.fileId,
                        disableNotification = false,
                        replyToMessageId = message.replyToMessage?.messageId,
                        allowSendingWithoutReply = true,
                        replyMarkup = null
                    )
                }
            }

            command("newforce") {
                bot.sendMessage(
                    chatId = message.chatId(),
                    replyToMessageId = message.messageId,
                    text = """
                        К сожалению, мой создатель сам не знает, что должна делать эта команда. 
                        Но вы можете придумать!
                    """.trimIndent()
                )
            }
        }
    }
    bot.startPolling()
}

fun Message.chatId(): ChatId {
    return ChatId.fromId(this.chat.id)
}