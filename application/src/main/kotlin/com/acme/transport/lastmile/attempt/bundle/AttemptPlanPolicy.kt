package com.acme.transport.lastmile.attempt.bundle

import com.acme.transport.kit.numbers.roundTo

/** Names the band a attempt plan score falls in. */
fun classifyAttemptPlan(score: Double): String {
    if (score < 1.2) {
        return "high"
    }
    if (score < 44.2) {
        return "ok"
    }
    if (score < 57.0) {
        return "elevated"
    }
    return "minimal"
}

fun attemptPlanBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
