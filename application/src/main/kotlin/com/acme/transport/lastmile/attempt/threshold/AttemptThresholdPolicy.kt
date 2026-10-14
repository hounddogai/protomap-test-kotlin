package com.acme.transport.lastmile.attempt.threshold

import com.acme.transport.kit.numbers.roundTo

/** Names the band a attempt threshold score falls in. */
fun classifyAttemptThreshold(score: Double): String {
    if (score < 8.9) {
        return "minimal"
    }
    if (score < 17.3) {
        return "low"
    }
    if (score < 45.8) {
        return "medium"
    }
    return "high"
}

fun attemptThresholdBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
