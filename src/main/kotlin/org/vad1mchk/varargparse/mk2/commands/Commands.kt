package org.vad1mchk.varargparse.mk2.commands

import com.github.kotlintelegrambot.dispatcher.handlers.*
import com.github.kotlintelegrambot.entities.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.vad1mchk.varargparse.mk2.config.Config
import org.vad1mchk.varargparse.mk2.database.tables.Quotes
import org.vad1mchk.varargparse.mk2.entities.Interjection
import org.vad1mchk.varargparse.mk2.util.*

private const val NEW_CHAT_MEMBER_LIST_COUNT = 5
private const val DEFAULT_LAST_QUOTES_COUNT = 5

private val COMMAND_REGEX = Regex("^\\s*/[0-9a-zA-Z_]*")

private val GET_QUOTES_COMMAND_REGEX = Regex(COMMAND_REGEX.pattern + "\\s+(\\d+)")

val greet: HandleNewChatMembers = {
    val newMembersNames = newChatMembers
        .map { user -> user.mentionMarkdown(firstNameOnly = true) }
        .joinToString(
            limit = NEW_CHAT_MEMBER_LIST_COUNT,
            truncated = " и ещё ${newChatMembers.size - NEW_CHAT_MEMBER_LIST_COUNT}"
        )

    bot.sendMessage(
        chatId = message.chatId(),
        replyToMessageId = message.messageId,
        text = """
            Привет, ${newMembersNames}!
            
            Пожалуйста, представьтесь и прочитайте правила клуба (в закрепе).
            """.trimIndent(),
        parseMode = ParseMode.MARKDOWN
    )
}

val startCommand: HandleCommand = {
    bot.sendMessage(
        chatId = message.chatId(),
        replyToMessageId = message.messageId,
        text = """
            *Привет,* ${update.message?.from?.mentionMarkdown() ?: "незнакомец"}*!*
            
            Меня зовут ${bot.getMe().getOrNull()?.fullName ?: "незнакомец"}.
            
            Напиши команду /help или открой моё меню, чтобы посмотреть доступные команды.
        """.trimIndent(),
        parseMode = ParseMode.MARKDOWN
    )
}

val helpCommand: HandleCommand = {
    bot.sendMessage(
        chatId = message.chatId(),
        replyToMessageId = message.messageId,
        text = bot
            .getMyCommands()
            .getOrNull()
            ?.map { cmd -> "/${cmd.command}: ${cmd.description}" }
            ?.joinToString("\n")
            ?: "Не удалось получить список команд.",
    )
}

val getQuoteCommand: HandleCommand = {
    val quoteResultRow = transaction {
        Quotes.selectAll()
            .orderBy(Random())
            .limit(1)
            .firstOrNull()
    }

    if (quoteResultRow != null) {
        val author = quoteResultRow[Quotes.authorName]
        val text = quoteResultRow[Quotes.quote]

        bot.sendMessage(
            chatId = message.chatId(),
            replyToMessageId = message.messageId,
            text = """
                $text
                
                ${'\u00a9'} $author
            """.trimIndent()
        )
    } else {
        bot.sendMessage(
            chatId = message.chatId(),
            replyToMessageId = message.messageId,
            text = "Ни одной цитаты в таблице не найдено."
        )
    }
}

val getLastFewQuotesCommand: HandleCommand = {
    val quoteCountToFind = message.text?.let {
        GET_QUOTES_COMMAND_REGEX.find(it)?.groups?.also { println(it) }?.get(1)?.value?.toInt()
    } ?: DEFAULT_LAST_QUOTES_COUNT

    if (quoteCountToFind == 0) {
        bot.sendMessage(
            chatId = message.chatId(),
            replyToMessageId = message.messageId,
            text = "Вы не попросили найти ни одной цитаты, поиск не требуется."
        )
    } else {
        val quotes = transaction {
            Quotes.selectAll()
                .orderBy(Quotes.creationDateTime, SortOrder.DESC_NULLS_LAST)
                .limit(quoteCountToFind)
                .toList()
        }

        if (quotes.isNotEmpty()) {
            val builder = StringBuilder()
            builder.append("Несколько последних цитат (${quotes.size}):")

            for (quoteResultRow in quotes) {
                val author = quoteResultRow[Quotes.authorName]
                val text = quoteResultRow[Quotes.quote]
                val creationDateTime = quoteResultRow[Quotes.creationDateTime]

                builder.append("\n\n")
                builder.append("""
                $text
                ${'\u00a9'} $author (загружено ${creationDateTime.javaFormat(defaultDateTimeFormatter())})
                """.trimIndent())
            }

            bot.sendMessage(
                chatId = message.chatId(),
                replyToMessageId = message.messageId,
                text = builder.toString().trim()
            )
        } else {
            bot.sendMessage(
                chatId = message.chatId(),
                replyToMessageId = message.messageId,
                text = "Ни одной цитаты в таблице не найдено."
            )
        }
    }
}

val deleteQuotesCommand: HandleCommand = {
    val submitterId = message.from?.id ?: throw IllegalArgumentException("Невозможно определить отправителя сообщения.")

    val deletedRowCount = transaction {
        Quotes.deleteWhere { Quotes.submitterId eq submitterId }
    }

    bot.sendMessage(
        chatId = message.chatId(),
        replyToMessageId = message.messageId,
        text = "Все ваши цитаты успешно удалены (количество: $deletedRowCount)"
    )
}

fun interjectionCommand(interjection: Interjection): HandleCommand = {
    val fileId = Config.publicConfig.interjections[interjection]?.fileIds?.get("sticker")
        ?: throw IllegalStateException("Sticker ID для выкрика $interjection не найден.")

    bot.sendSticker(
        chatId = message.chatId(),
        sticker = fileId,
        replyToMessageId = message.replyToMessage?.messageId,
        replyMarkup = null
    )
}