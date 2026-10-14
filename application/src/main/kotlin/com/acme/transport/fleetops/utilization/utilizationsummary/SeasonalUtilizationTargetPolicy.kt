package com.acme.transport.fleetops.utilization.utilizationsummary

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun seasonalUtilizationTargetBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalUtilizationTarget(): Int {
    val policy = defaultPolicy("seasonal-utilization-target")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a seasonal utilization target score falls in. */
fun classifySeasonalUtilizationTarget(score: Double): String {
    if (score < 87.5) {
        return "critical"
    }
    if (score < 87.8) {
        return "medium"
    }
    return "elevated"
}
