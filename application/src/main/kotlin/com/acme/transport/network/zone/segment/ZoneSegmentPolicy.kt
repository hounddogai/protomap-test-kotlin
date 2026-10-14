package com.acme.transport.network.zone.segment

import com.acme.transport.kit.numbers.roundTo

fun zoneSegmentBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a zone segment code in its canonical ZS- form. */
fun normalizeZoneSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ZS-")) {
        return clean
    }
    return "ZS-${clean.replace(" ", "-")}"
}

fun zoneSegmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
