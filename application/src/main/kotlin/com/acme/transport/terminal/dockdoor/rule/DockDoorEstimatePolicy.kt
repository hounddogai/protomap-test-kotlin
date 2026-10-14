package com.acme.transport.terminal.dockdoor.rule

import com.acme.transport.kit.numbers.roundTo

/** Names the band a dock door estimate score falls in. */
fun classifyDockDoorEstimate(score: Double): String {
    if (score < 21.2) {
        return "elevated"
    }
    if (score < 29.4) {
        return "medium"
    }
    if (score < 53.1) {
        return "minimal"
    }
    if (score < 60.4) {
        return "low"
    }
    return "high"
}

fun dockDoorEstimateBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
