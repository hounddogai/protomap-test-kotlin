package com.acme.transport.fleetops.tire.tirebudget

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun rollingTireSummaryBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingTireSummary(): Int {
    val policy = defaultPolicy("rolling-tire-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
