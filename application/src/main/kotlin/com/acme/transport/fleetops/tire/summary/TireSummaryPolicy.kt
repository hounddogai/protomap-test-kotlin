package com.acme.transport.fleetops.tire.summary

import com.acme.transport.kit.numbers.roundTo

/** Names the band a tire summary score falls in. */
fun classifyTireSummary(score: Double): String {
    if (score < 3.2) {
        return "elevated"
    }
    if (score < 94.0) {
        return "watch"
    }
    if (score < 98.5) {
        return "high"
    }
    return "low"
}

fun tireSummaryBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
