package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun cappedTenderClaimWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.76
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun cappedTenderClaimBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a capped tender claim score falls in. */
fun classifyCappedTenderClaim(score: Double): String {
    if (score < 14.5) {
        return "critical"
    }
    if (score < 49.2) {
        return "watch"
    }
    if (score < 58.2) {
        return "low"
    }
    if (score < 75.7) {
        return "medium"
    }
    return "ok"
}
