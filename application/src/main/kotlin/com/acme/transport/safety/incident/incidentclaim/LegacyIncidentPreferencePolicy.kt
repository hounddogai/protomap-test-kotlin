package com.acme.transport.safety.incident.incidentclaim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun legacyIncidentPreferenceBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun legacyIncidentPreferenceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

fun mergeLegacyIncidentPreferenceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a legacy incident preference score falls in. */
fun classifyLegacyIncidentPreference(score: Double): String {
    if (score < 4.0) {
        return "high"
    }
    if (score < 17.9) {
        return "low"
    }
    if (score < 27.2) {
        return "elevated"
    }
    if (score < 49.9) {
        return "critical"
    }
    return "ok"
}
