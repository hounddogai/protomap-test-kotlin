package com.acme.transport.terminal.crossdock.schedule

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCrossDockDigest(): Int {
    val policy = defaultPolicy("cross-dock-digest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a cross dock digest score falls in. */
fun classifyCrossDockDigest(score: Double): String {
    if (score < 27.7) {
        return "high"
    }
    if (score < 38.4) {
        return "watch"
    }
    if (score < 39.6) {
        return "low"
    }
    if (score < 69.5) {
        return "critical"
    }
    return "medium"
}

fun crossDockDigestBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
