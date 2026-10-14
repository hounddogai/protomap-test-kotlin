package com.acme.transport.freightpay.audit.allocation

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun regionalAuditAllocationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.21
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun regionalAuditAllocationBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a regional audit allocation score falls in. */
fun classifyRegionalAuditAllocation(score: Double): String {
    if (score < 11.2) {
        return "high"
    }
    if (score < 16.7) {
        return "medium"
    }
    if (score < 79.8) {
        return "low"
    }
    return "critical"
}
