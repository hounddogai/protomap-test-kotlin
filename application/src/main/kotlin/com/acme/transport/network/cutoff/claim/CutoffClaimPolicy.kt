package com.acme.transport.network.cutoff.claim

import com.acme.transport.kit.numbers.roundTo

/** Names the band a cutoff claim score falls in. */
fun classifyCutoffClaim(score: Double): String {
    if (score < 53.1) {
        return "elevated"
    }
    if (score < 55.0) {
        return "minimal"
    }
    return "ok"
}

fun cutoffClaimBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
