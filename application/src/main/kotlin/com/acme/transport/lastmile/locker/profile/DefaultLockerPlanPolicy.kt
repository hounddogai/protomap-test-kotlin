package com.acme.transport.lastmile.locker.profile

import com.acme.transport.kit.numbers.roundTo

fun defaultLockerPlanBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a default locker plan score falls in. */
fun classifyDefaultLockerPlan(score: Double): String {
    if (score < 60.1) {
        return "watch"
    }
    if (score < 76.1) {
        return "minimal"
    }
    return "medium"
}
