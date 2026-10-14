package com.acme.transport.safety.violation.violationpolicy

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictViolationPolicy(): Int {
    val policy = defaultPolicy("strict-violation-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 85568.0, in order. */
fun strictViolationPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 85568.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a strict violation policy score falls in. */
fun classifyStrictViolationPolicy(score: Double): String {
    if (score < 58.9) {
        return "low"
    }
    if (score < 69.1) {
        return "watch"
    }
    if (score < 95.9) {
        return "high"
    }
    return "minimal"
}
