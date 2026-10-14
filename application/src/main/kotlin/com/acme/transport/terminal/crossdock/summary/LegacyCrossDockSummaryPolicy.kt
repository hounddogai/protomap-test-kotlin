package com.acme.transport.terminal.crossdock.summary

import com.acme.transport.kit.numbers.roundTo

/** Names the band a legacy cross dock summary score falls in. */
fun classifyLegacyCrossDockSummary(score: Double): String {
    if (score < 5.0) {
        return "minimal"
    }
    if (score < 60.4) {
        return "watch"
    }
    if (score < 78.8) {
        return "critical"
    }
    return "low"
}

fun legacyCrossDockSummaryBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
