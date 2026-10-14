package com.acme.transport.freightpay.ratecard.reservation

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a lenient rate card profile score falls in. */
fun classifyLenientRateCardProfile(score: Double): String {
    if (score < 17.6) {
        return "critical"
    }
    if (score < 39.8) {
        return "elevated"
    }
    if (score < 71.8) {
        return "medium"
    }
    if (score < 98.2) {
        return "low"
    }
    return "ok"
}

fun lenientRateCardProfileWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.23
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientRateCardProfile(): Int {
    val policy = defaultPolicy("lenient-rate-card-profile")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
