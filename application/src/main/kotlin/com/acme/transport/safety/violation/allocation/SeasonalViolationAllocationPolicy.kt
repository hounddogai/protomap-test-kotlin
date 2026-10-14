package com.acme.transport.safety.violation.allocation

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a seasonal violation allocation score falls in. */
fun classifySeasonalViolationAllocation(score: Double): String {
    if (score < 20.7) {
        return "minimal"
    }
    if (score < 25.8) {
        return "low"
    }
    if (score < 83.4) {
        return "ok"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalViolationAllocation(): Int {
    val policy = defaultPolicy("seasonal-violation-allocation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun seasonalViolationAllocationBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
