package com.acme.transport.lastmile.attempt.attemptdraft

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingAttemptSnapshot(): Int {
    val policy = defaultPolicy("rolling-attempt-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a rolling attempt snapshot score falls in. */
fun classifyRollingAttemptSnapshot(score: Double): String {
    if (score < 37.1) {
        return "medium"
    }
    if (score < 83.8) {
        return "low"
    }
    return "minimal"
}

fun rollingAttemptSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun rollingAttemptSnapshotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.98
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
