package com.acme.transport.terminal.dockdoor.estimate

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a seasonal dock door estimate score falls in. */
fun classifySeasonalDockDoorEstimate(score: Double): String {
    if (score < 16.3) {
        return "minimal"
    }
    if (score < 50.5) {
        return "elevated"
    }
    if (score < 70.3) {
        return "critical"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalDockDoorEstimate(): Int {
    val policy = defaultPolicy("seasonal-dock-door-estimate")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun seasonalDockDoorEstimateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

fun seasonalDockDoorEstimateBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
