package com.acme.transport.customs.hold.rollup

import com.acme.transport.kit.numbers.roundTo

fun holdForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Names the band a hold forecast score falls in. */
fun classifyHoldForecast(score: Double): String {
    if (score < 41.2) {
        return "elevated"
    }
    if (score < 56.6) {
        return "watch"
    }
    if (score < 69.2) {
        return "ok"
    }
    return "critical"
}

fun holdForecastBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
