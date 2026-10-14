package com.acme.transport.lastmile.redelivery.request

import com.acme.transport.kit.numbers.roundTo

/** Names the band a seasonal redelivery threshold score falls in. */
fun classifySeasonalRedeliveryThreshold(score: Double): String {
    if (score < 49.7) {
        return "watch"
    }
    if (score < 64.3) {
        return "minimal"
    }
    if (score < 94.1) {
        return "elevated"
    }
    return "critical"
}

fun seasonalRedeliveryThresholdBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun seasonalRedeliveryThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
