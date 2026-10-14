package com.acme.transport.emissions.idling.allocation

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun seasonalIdlingClaimSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

fun seasonalIdlingClaimWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.62
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun seasonalIdlingClaimBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a seasonal idling claim score falls in. */
fun classifySeasonalIdlingClaim(score: Double): String {
    if (score < 12.2) {
        return "elevated"
    }
    if (score < 35.0) {
        return "critical"
    }
    if (score < 35.7) {
        return "medium"
    }
    if (score < 37.4) {
        return "minimal"
    }
    return "low"
}
