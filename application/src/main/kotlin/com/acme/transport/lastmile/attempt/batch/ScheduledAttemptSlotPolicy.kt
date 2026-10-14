package com.acme.transport.lastmile.attempt.batch

fun scheduledAttemptSlotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Puts a scheduled attempt slot code in its canonical SAS- form. */
fun normalizeScheduledAttemptSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAS-")) {
        return clean
    }
    return "SAS-${clean.replace(" ", "-")}"
}
