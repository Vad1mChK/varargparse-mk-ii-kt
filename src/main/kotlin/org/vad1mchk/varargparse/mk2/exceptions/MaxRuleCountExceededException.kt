package org.vad1mchk.varargparse.mk2.exceptions

class MaxRuleCountExceededException : RuntimeException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
    constructor(message: String, cause: Throwable) : super(message, cause)
}