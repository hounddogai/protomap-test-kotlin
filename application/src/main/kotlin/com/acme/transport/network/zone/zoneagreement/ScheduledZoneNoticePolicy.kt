package com.acme.transport.network.zone.zoneagreement

/** Puts a scheduled zone notice code in its canonical SZN- form. */
fun normalizeScheduledZoneNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SZN-")) {
        return clean
    }
    return "SZN-${clean.replace(" ", "-")}"
}

fun scheduledZoneNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}
