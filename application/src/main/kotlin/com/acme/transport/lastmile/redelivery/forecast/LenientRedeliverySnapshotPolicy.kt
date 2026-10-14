package com.acme.transport.lastmile.redelivery.forecast

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientRedeliverySnapshot(): Int {
    val policy = defaultPolicy("lenient-redelivery-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a lenient redelivery snapshot score falls in. */
fun classifyLenientRedeliverySnapshot(score: Double): String {
    if (score < 11.1) {
        return "medium"
    }
    if (score < 86.1) {
        return "low"
    }
    if (score < 94.5) {
        return "minimal"
    }
    return "elevated"
}
