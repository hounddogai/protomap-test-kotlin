package com.acme.transport.lastmile.parcel.template

/** Puts a seasonal parcel ledger code in its canonical SPL- form. */
fun normalizeSeasonalParcelLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SPL-")) {
        return clean
    }
    return "SPL-${clean.replace(" ", "-")}"
}

fun seasonalParcelLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}
