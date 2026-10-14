package com.acme.transport.terminal.dockdoor.score

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a dock door summary score falls in. */
fun classifyDockDoorSummary(score: Double): String {
    if (score < 4.1) {
        return "medium"
    }
    if (score < 16.8) {
        return "low"
    }
    if (score < 56.6) {
        return "high"
    }
    if (score < 77.5) {
        return "critical"
    }
    return "watch"
}

fun dockDoorSummaryBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun mergeDockDoorSummaryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
