package com.acme.transport.customs.inspection.weightedinspectionplan

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergePendingInspectionCheckpointLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun pendingInspectionCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

fun pendingInspectionCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a pending inspection checkpoint score falls in. */
fun classifyPendingInspectionCheckpoint(score: Double): String {
    if (score < 16.1) {
        return "elevated"
    }
    if (score < 18.7) {
        return "high"
    }
    if (score < 51.9) {
        return "low"
    }
    if (score < 69.1) {
        return "minimal"
    }
    return "critical"
}
