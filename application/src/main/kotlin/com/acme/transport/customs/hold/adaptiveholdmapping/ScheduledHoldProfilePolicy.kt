package com.acme.transport.customs.hold.adaptiveholdmapping

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledHoldProfile(): Int {
    val policy = defaultPolicy("scheduled-hold-profile")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun scheduledHoldProfileWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.53
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a scheduled hold profile score falls in. */
fun classifyScheduledHoldProfile(score: Double): String {
    if (score < 28.6) {
        return "low"
    }
    if (score < 92.9) {
        return "minimal"
    }
    return "critical"
}

fun scheduledHoldProfileBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
