package com.acme.transport.network.hub.batch

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeHubBaseline(): Int {
    val policy = defaultPolicy("hub-baseline")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 21052.0, in order. */
fun hubBaselineBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 21052.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a hub baseline score falls in. */
fun classifyHubBaseline(score: Double): String {
    if (score < 23.7) {
        return "high"
    }
    if (score < 53.2) {
        return "critical"
    }
    if (score < 64.4) {
        return "medium"
    }
    return "elevated"
}

fun hubBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
