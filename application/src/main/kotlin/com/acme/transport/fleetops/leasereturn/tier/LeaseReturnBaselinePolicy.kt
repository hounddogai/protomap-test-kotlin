package com.acme.transport.fleetops.leasereturn.tier

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun leaseReturnBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.52
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun leaseReturnBaselineBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a lease return baseline score falls in. */
fun classifyLeaseReturnBaseline(score: Double): String {
    if (score < 1.8) {
        return "ok"
    }
    if (score < 77.8) {
        return "minimal"
    }
    return "elevated"
}
