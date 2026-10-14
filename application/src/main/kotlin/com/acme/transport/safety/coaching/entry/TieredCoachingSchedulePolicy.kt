package com.acme.transport.safety.coaching.entry

import com.acme.transport.kit.numbers.roundTo

/** Names the band a tiered coaching schedule score falls in. */
fun classifyTieredCoachingSchedule(score: Double): String {
    if (score < 34.4) {
        return "minimal"
    }
    if (score < 39.6) {
        return "critical"
    }
    if (score < 68.4) {
        return "medium"
    }
    return "low"
}

fun tieredCoachingScheduleBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
