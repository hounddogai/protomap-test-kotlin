package com.acme.transport.customs.hold.quota

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun holdCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeHoldCheckpoint(): Int {
    val policy = defaultPolicy("hold-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a hold checkpoint score falls in. */
fun classifyHoldCheckpoint(score: Double): String {
    if (score < 24.1) {
        return "critical"
    }
    if (score < 34.6) {
        return "low"
    }
    if (score < 64.1) {
        return "elevated"
    }
    if (score < 90.1) {
        return "medium"
    }
    return "ok"
}
