package com.acme.transport.emissions.emissionfactor.digest

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun emissionFactorBundleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.31
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a emission factor bundle score falls in. */
fun classifyEmissionFactorBundle(score: Double): String {
    if (score < 2.2) {
        return "elevated"
    }
    if (score < 35.9) {
        return "watch"
    }
    if (score < 83.5) {
        return "medium"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeEmissionFactorBundle(): Int {
    val policy = defaultPolicy("emission-factor-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun emissionFactorBundleBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
