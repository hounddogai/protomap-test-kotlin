package com.acme.transport.emissions.fuelburn.target

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryFuelBurnReview(): Int {
    val policy = defaultPolicy("primary-fuel-burn-review")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a primary fuel burn review score falls in. */
fun classifyPrimaryFuelBurnReview(score: Double): String {
    if (score < 54.6) {
        return "ok"
    }
    if (score < 68.0) {
        return "low"
    }
    if (score < 74.1) {
        return "high"
    }
    return "watch"
}

fun primaryFuelBurnReviewWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.21
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
