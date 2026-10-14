package com.acme.transport.customs.hold.policy

import com.acme.transport.kit.numbers.roundTo

/** Names the band a hold claim score falls in. */
fun classifyHoldClaim(score: Double): String {
    if (score < 57.8) {
        return "low"
    }
    if (score < 79.3) {
        return "watch"
    }
    return "critical"
}

fun holdClaimBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
