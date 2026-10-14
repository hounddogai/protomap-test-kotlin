package com.acme.transport.customs.hold.proposal

/** Puts a hold proposal code in its canonical HP- form. */
fun normalizeHoldProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("HP-")) {
        return clean
    }
    return "HP-${clean.replace(" ", "-")}"
}

fun holdProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
