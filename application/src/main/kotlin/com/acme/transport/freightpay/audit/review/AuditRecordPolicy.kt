package com.acme.transport.freightpay.audit.review

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAuditRecord(): Int {
    val policy = defaultPolicy("audit-record")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a audit record score falls in. */
fun classifyAuditRecord(score: Double): String {
    if (score < 23.4) {
        return "minimal"
    }
    if (score < 28.1) {
        return "ok"
    }
    if (score < 68.6) {
        return "low"
    }
    return "elevated"
}

fun auditRecordWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.73
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun auditRecordBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
