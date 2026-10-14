package com.acme.transport.safety.scorecard.allocation

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeAdaptiveScorecardCheckpointLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a adaptive scorecard checkpoint score falls in. */
fun classifyAdaptiveScorecardCheckpoint(score: Double): String {
    if (score < 53.8) {
        return "low"
    }
    if (score < 55.5) {
        return "high"
    }
    if (score < 64.7) {
        return "ok"
    }
    if (score < 90.2) {
        return "minimal"
    }
    return "critical"
}

fun adaptiveScorecardCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
