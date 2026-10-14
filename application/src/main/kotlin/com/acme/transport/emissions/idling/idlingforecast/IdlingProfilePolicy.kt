package com.acme.transport.emissions.idling.idlingforecast

import com.acme.transport.kit.numbers.roundTo

fun idlingProfileBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a idling profile score falls in. */
fun classifyIdlingProfile(score: Double): String {
    if (score < 64.0) {
        return "elevated"
    }
    if (score < 77.8) {
        return "critical"
    }
    if (score < 89.5) {
        return "medium"
    }
    return "minimal"
}
