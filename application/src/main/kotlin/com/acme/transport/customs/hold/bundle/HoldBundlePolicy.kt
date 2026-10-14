package com.acme.transport.customs.hold.bundle

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun holdBundleBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeHoldBundle(): Int {
    val policy = defaultPolicy("hold-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a hold bundle score falls in. */
fun classifyHoldBundle(score: Double): String {
    if (score < 9.5) {
        return "high"
    }
    if (score < 56.4) {
        return "low"
    }
    if (score < 99.4) {
        return "elevated"
    }
    return "medium"
}
