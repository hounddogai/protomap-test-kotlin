package com.acme.transport.safety.coaching.milestone

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun rollingCoachingTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.76
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingCoachingTier(): Int {
    val policy = defaultPolicy("rolling-coaching-tier")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a rolling coaching tier score falls in. */
fun classifyRollingCoachingTier(score: Double): String {
    if (score < 7.6) {
        return "high"
    }
    if (score < 31.4) {
        return "elevated"
    }
    if (score < 88.5) {
        return "medium"
    }
    return "critical"
}

fun rollingCoachingTierBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
