package com.acme.transport.safety.incident.incidentbaseline

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeCappedIncidentBaselineLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun cappedIncidentBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Names the band a capped incident baseline score falls in. */
fun classifyCappedIncidentBaseline(score: Double): String {
    if (score < 69.0) {
        return "minimal"
    }
    if (score < 96.6) {
        return "medium"
    }
    return "ok"
}

fun cappedIncidentBaselineBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
