package com.acme.transport.terminal.traileryard.cycle

fun legacyTrailerYardCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Puts a legacy trailer yard cycle code in its canonical LTYC- form. */
fun normalizeLegacyTrailerYardCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LTYC-")) {
        return clean
    }
    return "LTYC-${clean.replace(" ", "-")}"
}
