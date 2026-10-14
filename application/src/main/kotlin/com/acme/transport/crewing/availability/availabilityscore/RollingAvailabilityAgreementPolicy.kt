package com.acme.transport.crewing.availability.availabilityscore

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun rollingAvailabilityAgreementWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.32
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun rollingAvailabilityAgreementBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingAvailabilityAgreement(): Int {
    val policy = defaultPolicy("rolling-availability-agreement")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a rolling availability agreement score falls in. */
fun classifyRollingAvailabilityAgreement(score: Double): String {
    if (score < 13.8) {
        return "elevated"
    }
    if (score < 42.9) {
        return "low"
    }
    if (score < 45.6) {
        return "ok"
    }
    if (score < 90.7) {
        return "watch"
    }
    return "high"
}
