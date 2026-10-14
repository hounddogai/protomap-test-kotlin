package com.acme.transport.lastmile.attempt.attemptdraft

/** Puts a global attempt draft code in its canonical GAD- form. */
fun normalizeGlobalAttemptDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GAD-")) {
        return clean
    }
    return "GAD-${clean.replace(" ", "-")}"
}

fun globalAttemptDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
