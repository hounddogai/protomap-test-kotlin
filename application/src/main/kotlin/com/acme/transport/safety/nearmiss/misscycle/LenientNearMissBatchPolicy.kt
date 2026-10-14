package com.acme.transport.safety.nearmiss.misscycle

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a lenient near miss batch score falls in. */
fun classifyLenientNearMissBatch(score: Double): String {
    if (score < 89.0) {
        return "critical"
    }
    if (score < 91.8) {
        return "elevated"
    }
    if (score < 94.8) {
        return "minimal"
    }
    return "high"
}

fun lenientNearMissBatchWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.01
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientNearMissBatch(): Int {
    val policy = defaultPolicy("lenient-near-miss-batch")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
