package com.acme.transport.emissions.evcharge.score

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a ev charge assignment score falls in. */
fun classifyEvChargeAssignment(score: Double): String {
    if (score < 6.0) {
        return "minimal"
    }
    if (score < 8.5) {
        return "ok"
    }
    if (score < 72.1) {
        return "low"
    }
    return "medium"
}

fun mergeEvChargeAssignmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
