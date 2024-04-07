package org.vad1mchk.varargparse.mk2

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.dispatcher.newChatMembers
import com.github.kotlintelegrambot.extensions.filters.Filter
import org.vad1mchk.varargparse.mk2.config.Config
import org.vad1mchk.varargparse.mk2.database.connectToDatabase
import org.vad1mchk.varargparse.mk2.database.initialize
import org.vad1mchk.varargparse.mk2.entities.Interjection
import org.vad1mchk.varargparse.mk2.handlers.*

fun main(args: Array<String>) {
    Config.loadPrivateConfigFromYaml(fileName = "private_config.yaml")
    Config.loadPublicConfigFromYaml(fileName = "public_config.yaml")

    Config.database = connectToDatabase()
    Config.database.initialize()

    val bot = bot {
        token = Config.privateConfig.token

        dispatch {
            command("start", startCommand)

            command("help", helpCommand)

            for (interjection in Interjection.entries) {
                command(interjection.name.lowercase(), interjectionCommand(interjection))
            }

            command("warn", warnCommand)
            callbackQuery(data = null, warnCallbackHandler)

            command("add_rule", addRuleCommand)

            command("stats", statsCommand)

            command("toggle_history", toggleHistoryCommand)

            command("clear_my_history", clearMyHistoryCommand)

            command("clear_my_history", clearAllHistoryCommand)

            newChatMembers(greet)

            message(
                Filter.Group and Filter.Custom { this.newChatMembers == null && this.leftChatMember == null },
                statsMessageHandler
            )

            message(
                Filter.Group and Filter.Custom { this.newChatMembers != null },
                statsJoinGroupHandler
            )

            message(
                Filter.Group and Filter.Custom { this.leftChatMember != null },
                statsLeaveGroupHandler
            )

            callbackQuery(data = null, greetCallbackHandler)
        }
    }

    bot.startPolling()
}