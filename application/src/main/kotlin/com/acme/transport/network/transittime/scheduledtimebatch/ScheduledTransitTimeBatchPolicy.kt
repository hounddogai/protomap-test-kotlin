package com.acme.transport.network.transittime.scheduledtimebatch

/** Puts a scheduled transit time batch code in its canonical STTB- form. */
fun normalizeScheduledTransitTimeBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("STTB-")) {
        return clean
    }
    return "STTB-${clean.replace(" ", "-")}"
}

fun scheduledTransitTimeBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}
