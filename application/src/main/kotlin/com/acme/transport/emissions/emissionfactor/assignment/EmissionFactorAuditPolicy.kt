package com.acme.transport.emissions.emissionfactor.assignment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun emissionFactorAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

fun emissionFactorAuditBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun emissionFactorAuditWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.68
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a emission factor audit score falls in. */
fun classifyEmissionFactorAudit(score: Double): String {
    if (score < 30.3) {
        return "high"
    }
    if (score < 73.5) {
        return "medium"
    }
    return "watch"
}
