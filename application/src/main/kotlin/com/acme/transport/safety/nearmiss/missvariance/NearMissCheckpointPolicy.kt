package com.acme.transport.safety.nearmiss.missvariance

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a near miss checkpoint score falls in. */
fun classifyNearMissCheckpoint(score: Double): String {
    if (score < 11.7) {
        return "medium"
    }
    if (score < 44.6) {
        return "watch"
    }
    if (score < 89.8) {
        return "minimal"
    }
    return "low"
}

fun mergeNearMissCheckpointLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
