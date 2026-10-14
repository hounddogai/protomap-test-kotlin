package com.acme.transport.safety.nearmiss.claim

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeNearMissPolicy(): Int {
    val policy = defaultPolicy("near-miss-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 66681.0, in order. */
fun nearMissPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 66681.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun nearMissPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Names the band a near miss policy score falls in. */
fun classifyNearMissPolicy(score: Double): String {
    if (score < 4.5) {
        return "low"
    }
    if (score < 62.3) {
        return "elevated"
    }
    return "medium"
}
