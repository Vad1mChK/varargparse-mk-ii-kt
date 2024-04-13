package org.vad1mchk.varargparse.mk2.handlers

import com.github.kotlintelegrambot.dispatcher.handlers.HandleCallbackQuery
import com.github.kotlintelegrambot.dispatcher.handlers.HandleCommand
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import org.vad1mchk.varargparse.mk2.config.Config
import org.vad1mchk.varargparse.mk2.database.*
import org.vad1mchk.varargparse.mk2.database.tables.Rules
import org.vad1mchk.varargparse.mk2.entities.Interjection
import org.vad1mchk.varargparse.mk2.exceptions.MaxRuleCountExceededException
import org.vad1mchk.varargparse.mk2.handlers.help.CommandHelpHolder
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

val helpCommand: HandleCommand = outer@{
    if (args.isEmpty()) {
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
        return@outer
    }

    if (args.size > 1) {
        bot.sendMessage(
            chatId = message.chatId(),
            replyToMessageId = message.messageId,
            text = """
                Вы ввели несколько названий команд: ${args.joinToString { "\"$it\"" }}.
                Введите только одно название команды (например, /help help), чтобы получить справку по этой команде.
                """.trimIndent(),
        )
        return@outer
    }

    val commandName = args[0]

    CommandHelpHolder[commandName]?.let { help ->
        val helpText = """
            |*Название*: `${help.name}`
            |
            |*Разрешения*: ${help.permissions.representation}
            |
            |*Описание*: 
            |${help.description}
            |*Использование*: 
            |${help.usage}
        """.trimMargin()

        bot.sendMessage(
            chatId = message.chatId(),
            replyToMessageId = message.messageId,
            text = helpText,
            parseMode = ParseMode.MARKDOWN
        )
        return@outer
    }

    bot.sendMessage(
        chatId = message.chatId(),
        replyToMessageId = message.messageId,
        text = """
            Не удалось найти команду с названием "$commandName".
            Пожалуйста, введите /help без аргументов, чтобы запросить список доступных команд.
            """.trimIndent()
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
                
                Пожалуйста, напишите команду как ответ на сообщение этого пользователя.
                """.trimIndent()
        )
        return@outer
    }

    val ruleButtons = Config.database.getRules(message.chat.id)
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

    Config.database.getRuleById(ruleId)?.let { rule ->
        val user = bot.getChatMember(ChatId.fromId(chatId), victimId).getOrNull()?.user
            ?: return@outer
        bot.sendMessage(
            chatId = ChatId.fromId(chatId),
            text = """
                ⚠️ ${user.mentionMarkdown()}, пожалуйста, вспомните наше *правило*:
                
                ${rule.description}
            """.trimIndent(),
            parseMode = ParseMode.MARKDOWN
        )
    }
}

val rulesCommand: HandleCommand = outer@{
    val rules = Config.database.getRules(message.chat.id)

    if (rules.isEmpty()) {
        bot.sendMessage(
            chatId = message.chatId(),
            replyToMessageId = message.messageId,
            text = "⭕\uFE0F Для этого чата не установлено ни одного правила."
        )
        return@outer
    }

    val rulesText = rules.mapIndexed { index, rule ->
        """
            ${index + 1}) *${rule.name}*
            *Описание*: ${rule.description}
            
        """.trimIndent()
    }.joinToString("\n")

    bot.sendMessage(
        chatId = message.chatId(),
        replyToMessageId = message.messageId,
        text = rulesText,
        parseMode = ParseMode.MARKDOWN
    )
}

val addRuleCommand: HandleCommand = adminProtectedCommand {
    try {
        message.text?.let { text ->
            NEW_RULE_REGEX.matchEntire(text)?.let { match ->
                if (match.groupValues.size != 4) {
                    throw IllegalArgumentException("Неправильное число параметров.")
                }

                val name = match.groupValues[2]
                val description = match.groupValues[3]

                val rule = Config.database.addRuleIfNotExceedsLimit(message.chat.id, name, description)

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

            is MaxRuleCountExceededException -> """
                Невозможно добавить правило.
                Достигнуто максимальное количество правил (${Rules.MAX_RULES_COUNT_PER_CHAT}).
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

val statsCommand: HandleCommand = outer@{
    val topUsers = Config.database.getTopUserIdsByChatId(
        message.chat.id,
        limit = 5
    )
        .filter { it.first != null && it.second > 0 }

    val joinCount = Config.database.getJoinCountByChatId(message.chat.id)
    val leaveCount = Config.database.getLeaveCountByChatId(message.chat.id)
    val messageCount = Config.database.getMessageCountByChatId(message.chat.id)

    val medals = arrayOf("\uD83E\uDD47", "\uD83E\uDD48", "\uD83E\uDD49", "\uD83C\uDFC5", "\uD83C\uDF96")

    val topUsersText = if (topUsers.isNotEmpty()) {
        topUsers.mapIndexed { index, (userId, count) ->
            if (userId == null) return@outer
            "${index + 1}) ${
                bot.getChatMember(message.chatId(), userId).getOrNull()?.user?.mentionMarkdown() ?: "незнакомец"
            }: $count сообщ. ${if (index < medals.size) medals[index] else ""}"
        }.joinToString("\n")
    } else {
        "Никто не написал ни одного сообщения или история была отключена или очищена."
    }

    bot.sendMessage(
        chatId = message.chatId(),
        replyToMessageId = message.messageId,
        text = """
            |*Статистика за последние $EVENTS_MAX_COUNT обновлений*:
            |
            |_Вступило в клуб_: $joinCount
            |
            |_Покинуло клуб_: $leaveCount
            |
            |_Топ-5 членов клуба по активности_:
            |
            |${topUsersText}
            |
            |_Общее число сообщений_: $messageCount
        """.trimMargin(),
        parseMode = ParseMode.MARKDOWN
    )
}

val toggleHistoryCommand: HandleCommand = adminProtectedCommand {
    val chatId = message.chat.id

    if (Config.database.isHistoryEnabledForChat(chatId)) {
        Config.database.setHistoryEnabledForChat(chatId, false)
        bot.sendMessage(
            chatId = message.chatId(),
            replyToMessageId = message.messageId,
            text = "Статистика сообщений отключена."
        )
    } else {
        Config.database.setHistoryEnabledForChat(chatId, true)
        bot.sendMessage(
            chatId = message.chatId(),
            replyToMessageId = message.messageId,
            text = "Статистика сообщений включена."
        )
    }
}

val clearMyHistoryCommand: HandleCommand = outer@{
    val userId = message.from?.id

    if (userId == null) {
        bot.sendMessage(
            chatId = message.chatId(),
            replyToMessageId = message.messageId,
            text = "Не удалось найти пользователя, для которого нужно очистить историю."
        )
        return@outer
    }

    Config.database.deleteEventsByUserIdAndChatId(userId, message.chat.id)

    bot.sendMessage(
        chatId = message.chatId(),
        replyToMessageId = message.messageId,
        text = "История ваших сообщений в клубе успешно очищена."
    )
}

val clearAllHistoryCommand: HandleCommand = adminProtectedCommand {
    Config.database.deleteEventsByChatId(message.chat.id)

    bot.sendMessage(
        chatId = message.chatId(),
        replyToMessageId = message.messageId,
        text = "История событий в клубе успешно очищена."
    )
}

fun adminProtectedCommand(command: HandleCommand): HandleCommand = {
    val isSenderAdmin = bot.getChatAdministrators(message.chatId())
        .getOrNull()
        ?.any { it.user.id == message.from?.id }
        ?: false

    if (isSenderAdmin) {
        command()
    } else {
        bot.sendMessage(
            chatId = message.chatId(),
            replyToMessageId = message.messageId,
            text = "\uD83D\uDD12 Команду может вызывать только админ группы.",
            parseMode = ParseMode.MARKDOWN
        )
    }
}