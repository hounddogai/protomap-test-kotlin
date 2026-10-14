package com.acme.transport.fleetops.leasereturn.returntier

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a rolling lease return tier score falls in. */
fun classifyRollingLeaseReturnTier(score: Double): String {
    if (score < 38.2) {
        return "elevated"
    }
    if (score < 49.7) {
        return "low"
    }
    return "high"
}

fun rollingLeaseReturnTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

fun rollingLeaseReturnTierBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun rollingLeaseReturnTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.42
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
