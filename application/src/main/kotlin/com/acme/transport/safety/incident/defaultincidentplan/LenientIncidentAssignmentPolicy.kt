package com.acme.transport.safety.incident.defaultincidentplan

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a lenient incident assignment score falls in. */
fun classifyLenientIncidentAssignment(score: Double): String {
    if (score < 32.1) {
        return "high"
    }
    if (score < 60.8) {
        return "medium"
    }
    if (score < 91.9) {
        return "critical"
    }
    return "minimal"
}

fun mergeLenientIncidentAssignmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
