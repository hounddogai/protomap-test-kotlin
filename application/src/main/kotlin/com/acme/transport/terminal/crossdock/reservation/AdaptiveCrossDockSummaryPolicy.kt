package com.acme.transport.terminal.crossdock.reservation

/** Puts a adaptive cross dock summary code in its canonical ACDS- form. */
fun normalizeAdaptiveCrossDockSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ACDS-")) {
        return clean
    }
    return "ACDS-${clean.replace(" ", "-")}"
}

fun adaptiveCrossDockSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
