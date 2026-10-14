package com.acme.transport.freightpay.audit.legacyauditpolicy

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun primaryAuditPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.69
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a primary audit policy score falls in. */
fun classifyPrimaryAuditPolicy(score: Double): String {
    if (score < 48.0) {
        return "high"
    }
    if (score < 86.1) {
        return "medium"
    }
    return "low"
}

fun primaryAuditPolicyBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
