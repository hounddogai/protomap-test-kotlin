package com.acme.transport.network.cutoff.proposal

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a rolling cutoff variance score falls in. */
fun classifyRollingCutoffVariance(score: Double): String {
    if (score < 14.4) {
        return "low"
    }
    if (score < 42.5) {
        return "high"
    }
    if (score < 74.7) {
        return "minimal"
    }
    return "medium"
}

fun rollingCutoffVarianceBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingCutoffVariance(): Int {
    val policy = defaultPolicy("rolling-cutoff-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
