package com.acme.transport.safety.coaching.digest

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a default coaching baseline score falls in. */
fun classifyDefaultCoachingBaseline(score: Double): String {
    if (score < 70.6) {
        return "medium"
    }
    if (score < 80.1) {
        return "low"
    }
    if (score < 83.7) {
        return "minimal"
    }
    if (score < 89.2) {
        return "elevated"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 41697.0, in order. */
fun defaultCoachingBaselineBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 41697.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultCoachingBaseline(): Int {
    val policy = defaultPolicy("default-coaching-baseline")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
