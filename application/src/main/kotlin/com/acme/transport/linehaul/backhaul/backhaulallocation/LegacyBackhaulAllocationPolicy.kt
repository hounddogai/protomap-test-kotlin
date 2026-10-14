package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun legacyBackhaulAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Names the band a legacy backhaul allocation score falls in. */
fun classifyLegacyBackhaulAllocation(score: Double): String {
    if (score < 12.9) {
        return "low"
    }
    if (score < 16.6) {
        return "ok"
    }
    if (score < 64.6) {
        return "medium"
    }
    return "watch"
}

fun legacyBackhaulAllocationBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun mergeLegacyBackhaulAllocationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
