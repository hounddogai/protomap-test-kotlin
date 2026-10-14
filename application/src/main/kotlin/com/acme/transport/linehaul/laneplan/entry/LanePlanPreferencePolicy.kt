package com.acme.transport.linehaul.laneplan.entry

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a lane plan preference score falls in. */
fun classifyLanePlanPreference(score: Double): String {
    if (score < 69.1) {
        return "high"
    }
    if (score < 86.9) {
        return "low"
    }
    return "elevated"
}

fun mergeLanePlanPreferenceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
