package com.acme.transport.linehaul.capacity.target

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun cappedCapacityThresholdBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun cappedCapacityThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

fun mergeCappedCapacityThresholdLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a capped capacity threshold score falls in. */
fun classifyCappedCapacityThreshold(score: Double): String {
    if (score < 6.1) {
        return "medium"
    }
    if (score < 15.8) {
        return "ok"
    }
    if (score < 33.4) {
        return "elevated"
    }
    if (score < 55.1) {
        return "critical"
    }
    return "high"
}
