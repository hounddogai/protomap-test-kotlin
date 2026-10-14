package com.acme.transport.safety.coaching.review

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun manualCoachingLedgerBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun manualCoachingLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.85
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a manual coaching ledger score falls in. */
fun classifyManualCoachingLedger(score: Double): String {
    if (score < 4.5) {
        return "high"
    }
    if (score < 18.7) {
        return "watch"
    }
    if (score < 54.6) {
        return "elevated"
    }
    if (score < 87.2) {
        return "ok"
    }
    return "minimal"
}
