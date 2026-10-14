package com.acme.transport.lastmile.attempt.attemptclaim

fun rollingAttemptTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Puts a rolling attempt tier code in its canonical RAT- form. */
fun normalizeRollingAttemptTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RAT-")) {
        return clean
    }
    return "RAT-${clean.replace(" ", "-")}"
}
