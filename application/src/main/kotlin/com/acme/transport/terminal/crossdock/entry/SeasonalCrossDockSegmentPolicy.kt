package com.acme.transport.terminal.crossdock.entry

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalCrossDockSegment(): Int {
    val policy = defaultPolicy("seasonal-cross-dock-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a seasonal cross dock segment score falls in. */
fun classifySeasonalCrossDockSegment(score: Double): String {
    if (score < 24.3) {
        return "medium"
    }
    if (score < 25.6) {
        return "ok"
    }
    if (score < 93.8) {
        return "low"
    }
    return "critical"
}

fun seasonalCrossDockSegmentBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
