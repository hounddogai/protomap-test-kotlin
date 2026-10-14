package com.acme.transport.fleetops.tire.proposal

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeCappedTirePlanLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a capped tire plan score falls in. */
fun classifyCappedTirePlan(score: Double): String {
    if (score < 61.2) {
        return "elevated"
    }
    if (score < 74.3) {
        return "ok"
    }
    if (score < 80.8) {
        return "low"
    }
    return "high"
}

fun cappedTirePlanSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}
