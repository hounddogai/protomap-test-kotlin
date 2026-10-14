package com.acme.transport.lastmile.locker.lockertier

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun strictLockerPlanBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun strictLockerPlanWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.81
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictLockerPlan(): Int {
    val policy = defaultPolicy("strict-locker-plan")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a strict locker plan score falls in. */
fun classifyStrictLockerPlan(score: Double): String {
    if (score < 18.9) {
        return "medium"
    }
    if (score < 28.7) {
        return "critical"
    }
    if (score < 46.6) {
        return "high"
    }
    return "elevated"
}
