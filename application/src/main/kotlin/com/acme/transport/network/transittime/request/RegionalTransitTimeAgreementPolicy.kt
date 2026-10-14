package com.acme.transport.network.transittime.request

/** Names the band a regional transit time agreement score falls in. */
fun classifyRegionalTransitTimeAgreement(score: Double): String {
    if (score < 6.9) {
        return "elevated"
    }
    if (score < 14.2) {
        return "medium"
    }
    if (score < 75.9) {
        return "high"
    }
    if (score < 94.1) {
        return "low"
    }
    return "minimal"
}

fun regionalTransitTimeAgreementSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Puts a regional transit time agreement code in its canonical RTTA- form. */
fun normalizeRegionalTransitTimeAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RTTA-")) {
        return clean
    }
    return "RTTA-${clean.replace(" ", "-")}"
}
