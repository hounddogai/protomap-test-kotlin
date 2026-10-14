package com.acme.transport.network.hub.hubmilestone

fun hubProfileSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Puts a hub profile code in its canonical HP- form. */
fun normalizeHubProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("HP-")) {
        return clean
    }
    return "HP-${clean.replace(" ", "-")}"
}
