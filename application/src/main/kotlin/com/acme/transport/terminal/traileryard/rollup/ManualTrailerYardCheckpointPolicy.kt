package com.acme.transport.terminal.traileryard.rollup

/** Puts a manual trailer yard checkpoint code in its canonical MTYC- form. */
fun normalizeManualTrailerYardCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MTYC-")) {
        return clean
    }
    return "MTYC-${clean.replace(" ", "-")}"
}

fun manualTrailerYardCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
