package com.acme.transport.freightpay.ratecard.rule

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun adaptiveRateCardReservationBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun adaptiveRateCardReservationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.63
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveRateCardReservation(): Int {
    val policy = defaultPolicy("adaptive-rate-card-reservation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a adaptive rate card reservation score falls in. */
fun classifyAdaptiveRateCardReservation(score: Double): String {
    if (score < 3.4) {
        return "watch"
    }
    if (score < 15.4) {
        return "low"
    }
    if (score < 57.0) {
        return "ok"
    }
    if (score < 63.2) {
        return "critical"
    }
    return "medium"
}
