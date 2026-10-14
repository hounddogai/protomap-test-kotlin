package com.acme.transport.lastmile.drop.forecast

fun dropLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Puts a drop ledger code in its canonical DL- form. */
fun normalizeDropLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DL-")) {
        return clean
    }
    return "DL-${clean.replace(" ", "-")}"
}
