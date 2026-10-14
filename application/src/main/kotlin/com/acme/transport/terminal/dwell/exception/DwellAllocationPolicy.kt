package com.acme.transport.terminal.dwell.exception

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeDwellAllocationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun dwellAllocationBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a dwell allocation score falls in. */
fun classifyDwellAllocation(score: Double): String {
    if (score < 12.9) {
        return "low"
    }
    if (score < 46.7) {
        return "elevated"
    }
    if (score < 72.7) {
        return "ok"
    }
    if (score < 83.2) {
        return "minimal"
    }
    return "high"
}
