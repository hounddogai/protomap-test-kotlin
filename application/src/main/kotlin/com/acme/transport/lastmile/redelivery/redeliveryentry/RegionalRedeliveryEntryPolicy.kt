package com.acme.transport.lastmile.redelivery.redeliveryentry

fun regionalRedeliveryEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Puts a regional redelivery entry code in its canonical RRE- form. */
fun normalizeRegionalRedeliveryEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RRE-")) {
        return clean
    }
    return "RRE-${clean.replace(" ", "-")}"
}
