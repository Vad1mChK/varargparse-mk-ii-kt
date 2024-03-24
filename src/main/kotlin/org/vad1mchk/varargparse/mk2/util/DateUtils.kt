package org.vad1mchk.varargparse.mk2.util

import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.LocalDateTime as KLocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

val RUSSIAN_LOCALE = Locale("ru", "RU")

fun KLocalDateTime.javaFormat(formatter: DateTimeFormatter) = formatter.format(this.toJavaLocalDateTime())

fun defaultDateTimeFormatter() = DateTimeFormatter.ofPattern(
    "d MMMM y, H:mm:ss",
    RUSSIAN_LOCALE
)