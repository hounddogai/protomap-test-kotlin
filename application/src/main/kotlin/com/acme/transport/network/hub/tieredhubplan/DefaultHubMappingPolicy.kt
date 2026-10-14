package com.acme.transport.network.hub.tieredhubplan

fun defaultHubMappingSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Puts a default hub mapping code in its canonical DHM- form. */
fun normalizeDefaultHubMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DHM-")) {
        return clean
    }
    return "DHM-${clean.replace(" ", "-")}"
}
