package com.acme.transport.freightpay.audit.manifest

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAuditEstimate(): Int {
    val policy = defaultPolicy("audit-estimate")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun auditEstimateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Counts how many amounts fit in a budget of 83269.0, in order. */
fun auditEstimateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 83269.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
