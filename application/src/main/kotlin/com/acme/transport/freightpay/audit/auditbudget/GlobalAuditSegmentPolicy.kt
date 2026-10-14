package com.acme.transport.freightpay.audit.auditbudget

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun globalAuditSegmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.5
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a global audit segment score falls in. */
fun classifyGlobalAuditSegment(score: Double): String {
    if (score < 16.1) {
        return "high"
    }
    if (score < 61.6) {
        return "low"
    }
    if (score < 72.0) {
        return "watch"
    }
    if (score < 77.9) {
        return "medium"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalAuditSegment(): Int {
    val policy = defaultPolicy("global-audit-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
