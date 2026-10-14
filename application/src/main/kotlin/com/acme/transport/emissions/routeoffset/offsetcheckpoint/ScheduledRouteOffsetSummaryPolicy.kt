package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Puts a scheduled route offset summary code in its canonical SROS- form. */
fun normalizeScheduledRouteOffsetSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SROS-")) {
        return clean
    }
    return "SROS-${clean.replace(" ", "-")}"
}

fun scheduledRouteOffsetSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

fun mergeScheduledRouteOffsetSummaryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun scheduledRouteOffsetSummaryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.72
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
