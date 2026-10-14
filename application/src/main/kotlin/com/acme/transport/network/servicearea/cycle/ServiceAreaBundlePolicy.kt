package com.acme.transport.network.servicearea.cycle

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 91554.0, in order. */
fun serviceAreaBundleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 91554.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a service area bundle score falls in. */
fun classifyServiceAreaBundle(score: Double): String {
    if (score < 14.1) {
        return "elevated"
    }
    if (score < 76.8) {
        return "low"
    }
    return "critical"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeServiceAreaBundle(): Int {
    val policy = defaultPolicy("service-area-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
