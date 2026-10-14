package com.acme.transport.customs.clearance.clearanceestimate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeLegacyClearanceTargetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun legacyClearanceTargetBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a legacy clearance target score falls in. */
fun classifyLegacyClearanceTarget(score: Double): String {
    if (score < 1.1) {
        return "critical"
    }
    if (score < 10.7) {
        return "low"
    }
    if (score < 27.1) {
        return "ok"
    }
    if (score < 68.4) {
        return "medium"
    }
    return "high"
}
