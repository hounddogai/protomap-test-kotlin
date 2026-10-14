package com.acme.transport.customs.hold.allocation

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 90290.0, in order. */
fun weightedHoldOverrideBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 90290.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a weighted hold override score falls in. */
fun classifyWeightedHoldOverride(score: Double): String {
    if (score < 2.3) {
        return "minimal"
    }
    if (score < 24.6) {
        return "elevated"
    }
    if (score < 46.7) {
        return "high"
    }
    if (score < 65.3) {
        return "critical"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedHoldOverride(): Int {
    val policy = defaultPolicy("weighted-hold-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
