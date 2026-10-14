package com.acme.transport.emissions.evcharge.segment

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackEvChargeClaim(): Int {
    val policy = defaultPolicy("fallback-ev-charge-claim")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 11181.0, in order. */
fun fallbackEvChargeClaimBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 11181.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a fallback ev charge claim score falls in. */
fun classifyFallbackEvChargeClaim(score: Double): String {
    if (score < 63.9) {
        return "ok"
    }
    if (score < 90.9) {
        return "critical"
    }
    if (score < 93.1) {
        return "medium"
    }
    return "minimal"
}
