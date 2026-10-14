package com.acme.transport.network.cutoff.allocation

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun rollingCutoffAllocationBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingCutoffAllocation(): Int {
    val policy = defaultPolicy("rolling-cutoff-allocation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun rollingCutoffAllocationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.95
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a rolling cutoff allocation score falls in. */
fun classifyRollingCutoffAllocation(score: Double): String {
    if (score < 4.3) {
        return "medium"
    }
    if (score < 45.1) {
        return "ok"
    }
    if (score < 50.1) {
        return "low"
    }
    if (score < 94.4) {
        return "critical"
    }
    return "watch"
}
