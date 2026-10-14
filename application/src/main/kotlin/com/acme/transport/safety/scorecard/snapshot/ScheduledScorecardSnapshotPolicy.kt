package com.acme.transport.safety.scorecard.snapshot

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a scheduled scorecard snapshot score falls in. */
fun classifyScheduledScorecardSnapshot(score: Double): String {
    if (score < 16.9) {
        return "minimal"
    }
    if (score < 31.5) {
        return "low"
    }
    if (score < 36.2) {
        return "ok"
    }
    if (score < 70.4) {
        return "critical"
    }
    return "elevated"
}

fun scheduledScorecardSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledScorecardSnapshot(): Int {
    val policy = defaultPolicy("scheduled-scorecard-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
