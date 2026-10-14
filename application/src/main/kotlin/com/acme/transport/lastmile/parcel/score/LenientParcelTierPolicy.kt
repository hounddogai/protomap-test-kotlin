package com.acme.transport.lastmile.parcel.score

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a lenient parcel tier score falls in. */
fun classifyLenientParcelTier(score: Double): String {
    if (score < 10.5) {
        return "ok"
    }
    if (score < 28.3) {
        return "low"
    }
    if (score < 43.0) {
        return "watch"
    }
    if (score < 82.1) {
        return "minimal"
    }
    return "elevated"
}

fun lenientParcelTierBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun lenientParcelTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.63
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientParcelTier(): Int {
    val policy = defaultPolicy("lenient-parcel-tier")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
