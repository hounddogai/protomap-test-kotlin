package com.acme.transport.linehaul.relay.weightedrelayrule

import com.acme.transport.kit.numbers.roundTo

/** Names the band a pending relay forecast score falls in. */
fun classifyPendingRelayForecast(score: Double): String {
    if (score < 25.2) {
        return "ok"
    }
    if (score < 39.8) {
        return "high"
    }
    if (score < 85.7) {
        return "medium"
    }
    if (score < 91.7) {
        return "critical"
    }
    return "low"
}

fun pendingRelayForecastBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun pendingRelayForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}
