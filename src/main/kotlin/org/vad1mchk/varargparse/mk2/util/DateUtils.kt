package org.vad1mchk.varargparse.mk2.util

import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.time.Duration
import kotlin.time.toJavaDuration
import kotlinx.datetime.LocalDateTime as KLocalDateTime

val RUSSIAN_LOCALE = Locale("ru", "RU")

fun KLocalDateTime.javaFormat(formatter: DateTimeFormatter) = formatter.format(this.toJavaLocalDateTime())

fun defaultDateTimeFormatter() = DateTimeFormatter.ofPattern(
    "d MMMM y, H:mm:ss",
    RUSSIAN_LOCALE
)

fun KLocalDateTime.Companion.fromUnixTimestamp(unixTimestamp: Long) = LocalDateTime.ofEpochSecond(
    unixTimestamp,
    0,
    OffsetDateTime.now().offset
).toKotlinLocalDateTime()

fun KLocalDateTime.Companion.now() = LocalDateTime.now().toKotlinLocalDateTime()

operator fun KLocalDateTime.minus(duration: Duration) = this.toJavaLocalDateTime()
    .minus(duration.toJavaDuration())
    .toKotlinLocalDateTime()