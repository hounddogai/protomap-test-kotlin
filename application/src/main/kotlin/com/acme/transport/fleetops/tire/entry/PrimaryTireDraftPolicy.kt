package com.acme.transport.fleetops.tire.entry

fun primaryTireDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Puts a primary tire draft code in its canonical PTD- form. */
fun normalizePrimaryTireDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PTD-")) {
        return clean
    }
    return "PTD-${clean.replace(" ", "-")}"
}
