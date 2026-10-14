package com.acme.transport.freightpay.audit.auditbudget

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun auditBatchBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAuditBatch(): Int {
    val policy = defaultPolicy("audit-batch")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 75823.0, in order. */
fun auditBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 75823.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a audit batch score falls in. */
fun classifyAuditBatch(score: Double): String {
    if (score < 26.0) {
        return "elevated"
    }
    if (score < 78.5) {
        return "watch"
    }
    return "high"
}
