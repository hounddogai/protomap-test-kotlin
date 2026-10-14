package com.acme.transport.safety.violation.forecast

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun globalViolationAgreementBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Counts how many amounts fit in a budget of 93609.0, in order. */
fun globalViolationAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 93609.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a global violation agreement score falls in. */
fun classifyGlobalViolationAgreement(score: Double): String {
    if (score < 35.0) {
        return "elevated"
    }
    if (score < 67.6) {
        return "minimal"
    }
    if (score < 89.6) {
        return "critical"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalViolationAgreement(): Int {
    val policy = defaultPolicy("global-violation-agreement")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
