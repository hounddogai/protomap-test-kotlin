package com.acme.transport.safety.coaching.adjustment

import com.acme.transport.kit.retry.defaultPolicy

fun seasonalCoachingSnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalCoachingSnapshot(): Int {
    val policy = defaultPolicy("seasonal-coaching-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a seasonal coaching snapshot score falls in. */
fun classifySeasonalCoachingSnapshot(score: Double): String {
    if (score < 9.6) {
        return "low"
    }
    if (score < 11.4) {
        return "watch"
    }
    if (score < 29.1) {
        return "critical"
    }
    if (score < 84.4) {
        return "ok"
    }
    return "high"
}
