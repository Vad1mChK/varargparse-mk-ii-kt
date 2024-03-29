package org.vad1mchk.varargparse.mk2.entities

import kotlinx.serialization.Serializable

/**
 * An [enum] class that represents interjections.
 *
 * @param fileId the unique id of the sticker file associated with the interjection
 * @param text the text of the interjection
 */
@Serializable
enum class Interjection(
    val fileId: String,
    val text: String
) {
    /**
     * An instance of [Interjection] with the given [fileId] and [text].
     */
    OBJECTION("CAACAgIAAxkBAAEK62Nlch09Qmi0cwAB-wc6es4LhmtPZWsAAsISAAL7orBIhQQHP2DECCszBA", "Objection!"),

    /**
     * An instance of [Interjection] with the given [fileId] and [text].
     */
    HOLD_IT("CAACAgIAAxkBAAEK62Vlch1Qx1I45fWDuf-JZgQyoLHZRAACCxIAApclsEhM7CMSW0ytVTME", "Hold it!"),

    /**
     * An instance of [Interjection] with the given [fileId] and [text].
     */
    TAKE_THAT("CAACAgIAAxkBAAEK62dlch1cXJZx1xAW-V_dt2ninfxR2wACmhAAAiWzqEgY6KJhqE3eATME", "Take that!"),

    /**
     * An instance of [Interjection] with the given [fileId] and [text].
     */
    EUREKA("CAACAgIAAxkBAAEK62llch1uFQGIvH1uzJQYt2jkYLXPwwAC9xEAAreIsUh0wAZZkvqwIDME", "Eureka!"),

    /**
     * An instance of [Interjection] with the given [fileId] and [text].
     */
    NOT_SO_FAST("CAACAgIAAxkBAAEK621lch16bf8qU0xvX_kzjayq5wZa1gAC_A4AAm8DqUg9cxT09XdkAjME", "Not so fast!"),

    /**
     * An instance of [Interjection] with the given [fileId] and [text].
     */
    OVERRULED("CAACAgIAAxkBAAEK629lch2CFWtZ3vLxKXREB-hx9pte4wACQhAAAp8LsEiond0F3P-IfTME", "Overruled!")
}