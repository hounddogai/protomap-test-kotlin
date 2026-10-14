package com.acme.transport.safety.scorecard.rule

import com.acme.transport.kit.numbers.roundTo

fun pendingScorecardForecastBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a pending scorecard forecast score falls in. */
fun classifyPendingScorecardForecast(score: Double): String {
    if (score < 27.5) {
        return "critical"
    }
    if (score < 49.2) {
        return "minimal"
    }
    if (score < 53.8) {
        return "high"
    }
    if (score < 64.2) {
        return "watch"
    }
    return "elevated"
}
