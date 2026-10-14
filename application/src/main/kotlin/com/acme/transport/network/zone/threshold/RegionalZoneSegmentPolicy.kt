package com.acme.transport.network.zone.threshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeRegionalZoneSegmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun regionalZoneSegmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Names the band a regional zone segment score falls in. */
fun classifyRegionalZoneSegment(score: Double): String {
    if (score < 7.5) {
        return "ok"
    }
    if (score < 41.9) {
        return "high"
    }
    if (score < 61.5) {
        return "elevated"
    }
    return "low"
}

/** Puts a regional zone segment code in its canonical RZS- form. */
fun normalizeRegionalZoneSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RZS-")) {
        return clean
    }
    return "RZS-${clean.replace(" ", "-")}"
}
