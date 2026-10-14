package com.acme.transport.customs.clearance.claim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeSeasonalClearanceOverrideLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a seasonal clearance override score falls in. */
fun classifySeasonalClearanceOverride(score: Double): String {
    if (score < 15.7) {
        return "low"
    }
    if (score < 23.9) {
        return "ok"
    }
    if (score < 24.9) {
        return "critical"
    }
    if (score < 26.2) {
        return "medium"
    }
    return "minimal"
}
