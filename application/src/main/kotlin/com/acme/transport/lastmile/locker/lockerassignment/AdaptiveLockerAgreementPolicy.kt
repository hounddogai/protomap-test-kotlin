package com.acme.transport.lastmile.locker.lockerassignment

fun adaptiveLockerAgreementSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Puts a adaptive locker agreement code in its canonical ALA- form. */
fun normalizeAdaptiveLockerAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ALA-")) {
        return clean
    }
    return "ALA-${clean.replace(" ", "-")}"
}
