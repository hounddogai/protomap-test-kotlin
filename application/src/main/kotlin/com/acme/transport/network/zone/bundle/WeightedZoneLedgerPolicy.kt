package com.acme.transport.network.zone.bundle

fun weightedZoneLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Puts a weighted zone ledger code in its canonical WZL- form. */
fun normalizeWeightedZoneLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WZL-")) {
        return clean
    }
    return "WZL-${clean.replace(" ", "-")}"
}
