package com.acme.transport.lastmile.attempt.attemptclaim

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun strictAttemptClaimWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.86
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun strictAttemptClaimBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a strict attempt claim score falls in. */
fun classifyStrictAttemptClaim(score: Double): String {
    if (score < 76.1) {
        return "elevated"
    }
    if (score < 98.2) {
        return "ok"
    }
    return "minimal"
}
