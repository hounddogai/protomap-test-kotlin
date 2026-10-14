package com.acme.transport.lastmile.parcel.claim

import com.acme.transport.kit.numbers.roundTo

fun parcelClaimBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a parcel claim score falls in. */
fun classifyParcelClaim(score: Double): String {
    if (score < 32.6) {
        return "elevated"
    }
    if (score < 42.2) {
        return "high"
    }
    if (score < 58.9) {
        return "critical"
    }
    if (score < 60.1) {
        return "low"
    }
    return "watch"
}
