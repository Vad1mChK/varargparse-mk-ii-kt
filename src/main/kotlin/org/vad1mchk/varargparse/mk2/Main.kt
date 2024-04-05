package org.vad1mchk.varargparse.mk2

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.newChatMembers
import org.vad1mchk.varargparse.mk2.commands.*
import org.vad1mchk.varargparse.mk2.config.Config
import org.vad1mchk.varargparse.mk2.database.connectToDatabase
import org.vad1mchk.varargparse.mk2.database.initializeDatabase
import org.vad1mchk.varargparse.mk2.entities.Interjection

fun main(args: Array<String>) {
    Config.loadPrivateConfigFromYaml(fileName = "private_config.yaml")
    Config.loadPublicConfigFromYaml(fileName = "public_config.yaml")

    val database = connectToDatabase()
    initializeDatabase(database)

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

            newChatMembers(greet)
            callbackQuery(data = null, greetCallbackHandler)
        }
    }

    bot.startPolling()
}