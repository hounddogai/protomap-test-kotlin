package com.acme.transport.crewing.availability.ledger

/** Puts a availability ledger code in its canonical AL- form. */
fun normalizeAvailabilityLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AL-")) {
        return clean
    }
    return "AL-${clean.replace(" ", "-")}"
}

fun availabilityLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
