package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeSeasonalRouteOffsetSegmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a seasonal route offset segment code in its canonical SROS- form. */
fun normalizeSeasonalRouteOffsetSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SROS-")) {
        return clean
    }
    return "SROS-${clean.replace(" ", "-")}"
}

/** Names the band a seasonal route offset segment score falls in. */
fun classifySeasonalRouteOffsetSegment(score: Double): String {
    if (score < 23.6) {
        return "elevated"
    }
    if (score < 36.9) {
        return "low"
    }
    if (score < 44.5) {
        return "minimal"
    }
    if (score < 50.0) {
        return "medium"
    }
    return "ok"
}
