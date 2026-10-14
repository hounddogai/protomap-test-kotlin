package com.acme.transport.network.zone.agreement

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun regionalZoneCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a regional zone checkpoint score falls in. */
fun classifyRegionalZoneCheckpoint(score: Double): String {
    if (score < 23.3) {
        return "critical"
    }
    if (score < 61.4) {
        return "ok"
    }
    if (score < 71.7) {
        return "minimal"
    }
    return "low"
}

fun mergeRegionalZoneCheckpointLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
