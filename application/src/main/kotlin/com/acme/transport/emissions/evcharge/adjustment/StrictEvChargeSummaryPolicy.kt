package com.acme.transport.emissions.evcharge.adjustment

import com.acme.transport.kit.numbers.roundTo

/** Names the band a strict ev charge summary score falls in. */
fun classifyStrictEvChargeSummary(score: Double): String {
    if (score < 26.3) {
        return "low"
    }
    if (score < 29.9) {
        return "elevated"
    }
    if (score < 51.8) {
        return "critical"
    }
    return "watch"
}

fun strictEvChargeSummaryBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
