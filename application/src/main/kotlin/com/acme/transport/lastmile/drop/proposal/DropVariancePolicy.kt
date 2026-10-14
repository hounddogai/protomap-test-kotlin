package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDropVariance(): Int {
    val policy = defaultPolicy("drop-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun dropVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.15
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun dropVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Names the band a drop variance score falls in. */
fun classifyDropVariance(score: Double): String {
    if (score < 25.7) {
        return "medium"
    }
    if (score < 41.8) {
        return "low"
    }
    return "high"
}
