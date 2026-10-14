package com.acme.transport.emissions.fuelburn.burnrequest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeRollingFuelBurnRequestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun rollingFuelBurnRequestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Names the band a rolling fuel burn request score falls in. */
fun classifyRollingFuelBurnRequest(score: Double): String {
    if (score < 1.7) {
        return "watch"
    }
    if (score < 28.2) {
        return "minimal"
    }
    if (score < 42.1) {
        return "high"
    }
    if (score < 68.0) {
        return "critical"
    }
    return "elevated"
}

fun rollingFuelBurnRequestBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
