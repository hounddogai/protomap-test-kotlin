package com.acme.transport.terminal.dwell.score

fun dwellRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Puts a dwell rollup code in its canonical DR- form. */
fun normalizeDwellRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DR-")) {
        return clean
    }
    return "DR-${clean.replace(" ", "-")}"
}
