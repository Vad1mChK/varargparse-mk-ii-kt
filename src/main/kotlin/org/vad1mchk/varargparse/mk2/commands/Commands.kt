package org.vad1mchk.varargparse.mk2.commands

import com.github.kotlintelegrambot.dispatcher.handlers.HandleCallbackQuery
import com.github.kotlintelegrambot.dispatcher.handlers.HandleCommand
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import org.vad1mchk.varargparse.mk2.config.Config
import org.vad1mchk.varargparse.mk2.database.addRule
import org.vad1mchk.varargparse.mk2.database.getRuleById
import org.vad1mchk.varargparse.mk2.database.getRules
import org.vad1mchk.varargparse.mk2.entities.Interjection
import org.vad1mchk.varargparse.mk2.util.chatId
import org.vad1mchk.varargparse.mk2.util.fullName
import org.vad1mchk.varargparse.mk2.util.mentionMarkdown
import java.sql.SQLException

private val NEW_RULE_REGEX = Regex("/([a-zA-Z0-9_]+(?:@[a-zA-Z0-9_]+)?)\\s+(.*)\\n\\s*((?:.|\\n)+)")
private val USERNAME_REGEX = Regex("@[_A-Za-z][_0-9A-Za-z]*")

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

val warnCommand: HandleCommand = outer@{
    val warnVictimUserId = message.replyToMessage?.from?.id

    if (warnVictimUserId == null) {
        bot.sendMessage(
            chatId = message.chatId(),
            replyToMessageId = message.messageId,
            text = """
                Не удалось найти пользователя, которого нужно предупредить.
                
                Пожалуйста, ответьте на сообщение этого пользователя или напишите его ник в качестве аргумента команды.
                
                Рассматриваться будет только первый написанный ник, ник имеет приоритет над ответом.
                """.trimIndent()
        )
        return@outer
    }

    val ruleButtons = getRules(Config.database, message.chat.id, 15)
        .ifEmpty {
            bot.sendMessage(
                chatId = message.chatId(),
                replyToMessageId = message.messageId,
                text = "⭕\uFE0F Для этого чата не установлено ни одного правила."
            )
            return@outer
        }
        .map {
            listOf(
                InlineKeyboardButton.CallbackData(
                    it.name, "warn,${message.chat.id},${warnVictimUserId},${it.id}"
                )
            )
        }

    val markup = InlineKeyboardMarkup.create(ruleButtons)

    bot.sendMessage(
        chatId = message.chatId(),
        replyToMessageId = message.messageId,
        text = "Выберите название правила:",
        replyMarkup = markup
    )
}

val warnCallbackHandler: HandleCallbackQuery = outer@{
    val data = callbackQuery.data.split(",")

    if (data.size != 4 || data[0] != "warn") {
        return@outer
    }

    val chatId = data[1].toLongOrNull() ?: return@outer
    val victimId = data[2].toLongOrNull() ?: return@outer
    val ruleId = data[3].toLongOrNull() ?: return@outer

    getRuleById(Config.database, ruleId)?.let { rule ->
        val user = bot.getChatMember(ChatId.fromId(chatId), victimId).getOrNull()?.user
            ?: return@outer
        bot.sendMessage(
            chatId = ChatId.fromId(chatId),
            text = """
                ⚠️ ${user.mentionMarkdown()}, вам вынесено предупреждение!
                
                По мнению сообщества, вы нарушили *правило*:
                
                ${rule.description}
            """.trimIndent(),
            parseMode = ParseMode.MARKDOWN
        )
    }
}

val addRuleCommand: HandleCommand = ownerProtectedCommand {
    try {
        message.text?.let { text ->
            NEW_RULE_REGEX.matchEntire(text)?.let { match ->
                if (match.groupValues.size != 4) {
                    throw IllegalArgumentException("Неправильное число параметров.")
                }

                val name = match.groupValues[2]
                val description = match.groupValues[3]

                addRule(Config.database, message.chat.id, name, description)

                bot.sendMessage(
                    chatId = message.chatId(),
                    replyToMessageId = message.messageId,
                    text = """Добавлено правило.
                        |
                        |*Название правила*:
                        |$name
                        |
                        |*Описание правила*:
                        |$description
                    """.trimMargin(),
                    parseMode = ParseMode.MARKDOWN
                )
            }
        } ?: throw IllegalArgumentException(
            "Не удалось получить текст сообщения или он представлен в неверном формате."
        )
    } catch (e: Exception) {
        val errorMessage = when (e) {
            is IllegalArgumentException -> """
                Неверный формат правила. Попробуйте использовать следующий формат: ```
                /add_rule <имя правила>
                <описание правила>
                ``` (не забудьте отбить строку между именем и описанием)
                """.trimIndent()

            is SQLException -> """
                Произошла ошибка при выполнении запроса к базе данных.
                Попробуйте повторить попытку позже.
                Также убедитесь, что правила с таким названием ещё нет и название правила не длиннее 64 символов.
                """.trimIndent()

            else -> "Произошла неизвестная ошибка."
        }

        bot.sendMessage(
            chatId = message.chatId(),
            replyToMessageId = message.messageId,
            text = errorMessage,
            parseMode = ParseMode.MARKDOWN
        )
    }

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

/**
 * Wrapper for [HandleCallbackQuery] that can be used as a lambda.
 * Restricts access to the wrapped commands only to the bot owner.
 *
 * @param command command to execute
 */
fun ownerProtectedCommand(command: HandleCommand): HandleCommand = {
    if (message.from?.id == Config.privateConfig.ownerId) {
        command()
    } else {
        bot.sendMessage(
            chatId = message.chatId(),
            replyToMessageId = message.messageId,
            text = "\uD83D\uDD12 Команду может вызывать только владелец бота.",
            parseMode = ParseMode.MARKDOWN
        )
    }
}