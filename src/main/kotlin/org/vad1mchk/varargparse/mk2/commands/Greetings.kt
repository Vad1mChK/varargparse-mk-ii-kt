package org.vad1mchk.varargparse.mk2.commands

import com.github.kotlintelegrambot.dispatcher.handlers.HandleCallbackQuery
import com.github.kotlintelegrambot.dispatcher.handlers.HandleNewChatMembers
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import org.vad1mchk.varargparse.mk2.util.chatId
import org.vad1mchk.varargparse.mk2.util.mentionMarkdown

private const val NEW_CHAT_MEMBER_LIST_COUNT = 5

private data class Question(
    val questionName: String,
    val questionText: String,
    val answers: Map<String, String>, // pairs of answer name and answer text
    val correctAnswerName: String
)

private val initialQuestion = Question(
    "initial",
    "Вы из *Университета ИТМО* и увлекаетесь _Ace Attorney_?",
    mapOf(
        "yes" to "Да",
        "no" to "Нет"
    ),
    "yes"
)

private val controlQuestions = listOf(
    Question(
        "control_1",
        "Как называется *уникальный 6-значный номер* человека в Университете ИТМО?",
        mapOf(
            "my_itmo_id" to "my.itmo ID",
            "itmo_id" to "ITMO ID",
            "isu" to "Номер ИСУ"
        ),
        "isu"
    ),
    Question(
        "control_2",
        "Где находится *главный* корпус ИТМО?",
        mapOf(
            "kronva" to "Кронверкский пр.",
            "lomo" to "ул. Ломоносова",
            "tchaika" to "ул. Чайковского"
        ),
        "kronva"
    )
)

val greet: HandleNewChatMembers = outer@{
    val newMembersNames = newChatMembers
        .map { user -> user.mentionMarkdown(firstNameOnly = true) }
        .joinToString(
            limit = NEW_CHAT_MEMBER_LIST_COUNT,
            truncated = " и ещё ${newChatMembers.size - NEW_CHAT_MEMBER_LIST_COUNT}"
        )

    val question = initialQuestion ?: return@outer

    val keyboardMarkup = InlineKeyboardMarkup.create(
        question.answers.map { answer ->
            InlineKeyboardButton.CallbackData(
                answer.value, "greet,${message.chat.id},${question.questionName},${answer.key}"
            )
        }
    )

    bot.sendMessage(
        chatId = message.chatId(),
        replyToMessageId = message.messageId,
        text = "Привет, ${newMembersNames}!".trimIndent(),
        parseMode = ParseMode.MARKDOWN
    )

    bot.sendMessage(
        chatId = message.chatId(),
        replyToMessageId = message.messageId,
        text = question.questionText,
        parseMode = ParseMode.MARKDOWN,
        replyMarkup = keyboardMarkup
    )
}

val greetCallbackHandler: HandleCallbackQuery = outer@{
    val data = callbackQuery.data.split(",")

    if (data.size != 4 || data[0] != "greet") {
        return@outer
    }

    println(data)

    val chatId = data[1].toLongOrNull() ?: return@outer
    val questionName = data[2]
    val answerName = data[3]

    val isInitial = questionName == "initial"

    val question = if (isInitial) initialQuestion else controlQuestions.firstOrNull {
        it.questionName == questionName
    } ?: return@outer

    if (answerName != question.correctAnswerName) {
        bot.sendMessage(
            chatId = ChatId.fromId(chatId),
            text = """
                ${"\uD83D\uDED1"} Неправильно. Попробуй... ещё... раз.
                
                Вернитесь к вопросу и нажмите кнопку правильного ответа.
            """.trimIndent(),
            parseMode = ParseMode.MARKDOWN
        )
        return@outer
    }

    if (isInitial) {
        val controlQuestion = controlQuestions.random()

        println(question)

        val keyboardMarkup = InlineKeyboardMarkup.create(
            controlQuestion.answers.map { answer ->
                listOf(InlineKeyboardButton.CallbackData(
                    answer.value, "greet,${chatId},${controlQuestion.questionName},${answer.key}"
                ))
            }.shuffled()
        )

        bot.sendMessage(
            chatId = ChatId.fromId(chatId),
            text = """
                *Контрольный вопрос*:
                
                ${controlQuestion.questionText}
            """.trimIndent(),
            parseMode = ParseMode.MARKDOWN,
            replyMarkup = keyboardMarkup
        )
    } else {
        bot.sendMessage(
            chatId = ChatId.fromId(chatId),
            text = """
                ✅ Отлично! Добро пожаловать в `java.lang.Objection`!
                
                Пожалуйста, прочитайте *правила клуба* в закрепе.
            """.trimIndent(),
            parseMode = ParseMode.MARKDOWN
        )
    }
}