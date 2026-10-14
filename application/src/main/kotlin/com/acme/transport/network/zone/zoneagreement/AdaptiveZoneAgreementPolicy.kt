package com.acme.transport.network.zone.zoneagreement

fun adaptiveZoneAgreementSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Puts a adaptive zone agreement code in its canonical AZA- form. */
fun normalizeAdaptiveZoneAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AZA-")) {
        return clean
    }
    return "AZA-${clean.replace(" ", "-")}"
}
