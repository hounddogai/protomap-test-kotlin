package com.acme.transport.lastmile.locker.rule

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun fallbackLockerBaselineBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackLockerBaseline(): Int {
    val policy = defaultPolicy("fallback-locker-baseline")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a fallback locker baseline score falls in. */
fun classifyFallbackLockerBaseline(score: Double): String {
    if (score < 43.6) {
        return "critical"
    }
    if (score < 83.6) {
        return "high"
    }
    return "low"
}
