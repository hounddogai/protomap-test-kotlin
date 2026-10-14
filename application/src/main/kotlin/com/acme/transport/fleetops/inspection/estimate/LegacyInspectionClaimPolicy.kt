package com.acme.transport.fleetops.inspection.estimate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a legacy inspection claim score falls in. */
fun classifyLegacyInspectionClaim(score: Double): String {
    if (score < 55.1) {
        return "watch"
    }
    if (score < 64.5) {
        return "elevated"
    }
    if (score < 72.5) {
        return "ok"
    }
    return "high"
}

fun mergeLegacyInspectionClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun legacyInspectionClaimBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
