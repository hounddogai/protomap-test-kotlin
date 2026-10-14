package com.acme.transport.network.cutoff.quota

import com.acme.transport.kit.numbers.roundTo

fun cutoffQuotaBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a cutoff quota score falls in. */
fun classifyCutoffQuota(score: Double): String {
    if (score < 82.6) {
        return "critical"
    }
    if (score < 94.8) {
        return "watch"
    }
    if (score < 95.8) {
        return "ok"
    }
    if (score < 98.2) {
        return "low"
    }
    return "minimal"
}
