package com.acme.transport.fleetops.inspection.globalinspectionscore

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a scheduled inspection score score falls in. */
fun classifyScheduledInspectionScore(score: Double): String {
    if (score < 10.2) {
        return "ok"
    }
    if (score < 47.3) {
        return "minimal"
    }
    if (score < 91.6) {
        return "high"
    }
    return "elevated"
}

fun mergeScheduledInspectionScoreLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
