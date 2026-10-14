package com.acme.transport.network.zone.recorditem

fun zoneRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Puts a zone record code in its canonical ZR- form. */
fun normalizeZoneRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ZR-")) {
        return clean
    }
    return "ZR-${clean.replace(" ", "-")}"
}

/** Names the band a zone record score falls in. */
fun classifyZoneRecord(score: Double): String {
    if (score < 24.5) {
        return "critical"
    }
    if (score < 49.8) {
        return "low"
    }
    if (score < 72.4) {
        return "medium"
    }
    if (score < 93.7) {
        return "elevated"
    }
    return "minimal"
}
