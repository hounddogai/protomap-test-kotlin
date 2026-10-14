package com.acme.transport.fleetops.leasereturn.draft

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun regionalLeaseReturnPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.67
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun regionalLeaseReturnPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun regionalLeaseReturnPolicyBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a regional lease return policy score falls in. */
fun classifyRegionalLeaseReturnPolicy(score: Double): String {
    if (score < 7.0) {
        return "elevated"
    }
    if (score < 42.9) {
        return "medium"
    }
    if (score < 67.8) {
        return "critical"
    }
    return "high"
}
