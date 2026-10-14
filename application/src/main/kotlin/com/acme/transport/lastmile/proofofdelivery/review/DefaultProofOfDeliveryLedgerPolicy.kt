package com.acme.transport.lastmile.proofofdelivery.review

fun defaultProofOfDeliveryLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Puts a default proof of delivery ledger code in its canonical DPODL- form. */
fun normalizeDefaultProofOfDeliveryLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DPODL-")) {
        return clean
    }
    return "DPODL-${clean.replace(" ", "-")}"
}
