package com.acme.transport.emissions.evcharge.forecast

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a fallback ev charge mapping score falls in. */
fun classifyFallbackEvChargeMapping(score: Double): String {
    if (score < 7.3) {
        return "watch"
    }
    if (score < 14.1) {
        return "medium"
    }
    if (score < 25.3) {
        return "elevated"
    }
    if (score < 36.8) {
        return "critical"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackEvChargeMapping(): Int {
    val policy = defaultPolicy("fallback-ev-charge-mapping")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun fallbackEvChargeMappingWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.88
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun fallbackEvChargeMappingBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
