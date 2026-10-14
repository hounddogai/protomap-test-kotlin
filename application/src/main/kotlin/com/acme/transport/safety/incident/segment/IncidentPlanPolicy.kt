package com.acme.transport.safety.incident.segment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeIncidentPlanLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a incident plan score falls in. */
fun classifyIncidentPlan(score: Double): String {
    if (score < 3.5) {
        return "watch"
    }
    if (score < 60.2) {
        return "medium"
    }
    if (score < 79.3) {
        return "critical"
    }
    if (score < 81.7) {
        return "elevated"
    }
    return "low"
}
