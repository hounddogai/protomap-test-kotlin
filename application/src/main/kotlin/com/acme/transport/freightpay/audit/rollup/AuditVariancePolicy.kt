package com.acme.transport.freightpay.audit.rollup

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun auditVarianceBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a audit variance score falls in. */
fun classifyAuditVariance(score: Double): String {
    if (score < 18.5) {
        return "high"
    }
    if (score < 53.5) {
        return "minimal"
    }
    if (score < 61.4) {
        return "watch"
    }
    if (score < 71.6) {
        return "ok"
    }
    return "critical"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAuditVariance(): Int {
    val policy = defaultPolicy("audit-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 81969.0, in order. */
fun auditVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 81969.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
