package com.acme.transport.terminal.dwell.variance

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a dwell variance score falls in. */
fun classifyDwellVariance(score: Double): String {
    if (score < 67.1) {
        return "minimal"
    }
    if (score < 82.6) {
        return "watch"
    }
    if (score < 90.5) {
        return "ok"
    }
    if (score < 90.7) {
        return "medium"
    }
    return "critical"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDwellVariance(): Int {
    val policy = defaultPolicy("dwell-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun dwellVarianceBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
