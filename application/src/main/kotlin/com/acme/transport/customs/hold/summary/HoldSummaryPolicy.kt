package com.acme.transport.customs.hold.summary

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun holdSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeHoldSummary(): Int {
    val policy = defaultPolicy("hold-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun holdSummaryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.51
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun holdSummaryBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
