package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a pending inspection checkpoint score falls in. */
fun classifyPendingInspectionCheckpoint(score: Double): String {
    if (score < 59.0) {
        return "high"
    }
    if (score < 82.6) {
        return "watch"
    }
    if (score < 97.4) {
        return "ok"
    }
    if (score < 99.2) {
        return "low"
    }
    return "minimal"
}

fun mergePendingInspectionCheckpointLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
