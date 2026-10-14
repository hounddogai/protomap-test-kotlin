package com.acme.transport.network.cutoff.draft

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun lenientCutoffSegmentBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a lenient cutoff segment score falls in. */
fun classifyLenientCutoffSegment(score: Double): String {
    if (score < 11.7) {
        return "medium"
    }
    if (score < 49.8) {
        return "ok"
    }
    if (score < 72.3) {
        return "elevated"
    }
    if (score < 98.1) {
        return "watch"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientCutoffSegment(): Int {
    val policy = defaultPolicy("lenient-cutoff-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun lenientCutoffSegmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.44
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
