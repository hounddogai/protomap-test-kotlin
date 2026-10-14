package com.acme.transport.crewing.availability.mapping

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Counts how many amounts fit in a budget of 67637.0, in order. */
fun rollingAvailabilityMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 67637.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun rollingAvailabilityMappingWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.82
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a rolling availability mapping score falls in. */
fun classifyRollingAvailabilityMapping(score: Double): String {
    if (score < 28.3) {
        return "minimal"
    }
    if (score < 30.6) {
        return "ok"
    }
    if (score < 74.7) {
        return "critical"
    }
    if (score < 84.2) {
        return "elevated"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingAvailabilityMapping(): Int {
    val policy = defaultPolicy("rolling-availability-mapping")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
