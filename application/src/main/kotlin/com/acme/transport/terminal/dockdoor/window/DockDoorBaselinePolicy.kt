package com.acme.transport.terminal.dockdoor.window

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeDockDoorBaselineLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun dockDoorBaselineBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a dock door baseline score falls in. */
fun classifyDockDoorBaseline(score: Double): String {
    if (score < 39.2) {
        return "medium"
    }
    if (score < 40.9) {
        return "elevated"
    }
    if (score < 64.9) {
        return "ok"
    }
    if (score < 77.9) {
        return "high"
    }
    return "low"
}
