package com.acme.transport.linehaul.capacity.threshold

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun fallbackCapacityBundleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.0
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a fallback capacity bundle score falls in. */
fun classifyFallbackCapacityBundle(score: Double): String {
    if (score < 10.9) {
        return "watch"
    }
    if (score < 26.9) {
        return "medium"
    }
    if (score < 43.2) {
        return "minimal"
    }
    if (score < 89.7) {
        return "elevated"
    }
    return "critical"
}

fun fallbackCapacityBundleBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackCapacityBundle(): Int {
    val policy = defaultPolicy("fallback-capacity-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
