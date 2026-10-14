package com.acme.transport.fleetops.utilization.rollup

import com.acme.transport.kit.numbers.roundTo

fun scheduledUtilizationForecastBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a scheduled utilization forecast score falls in. */
fun classifyScheduledUtilizationForecast(score: Double): String {
    if (score < 62.8) {
        return "elevated"
    }
    if (score < 71.5) {
        return "ok"
    }
    return "minimal"
}
