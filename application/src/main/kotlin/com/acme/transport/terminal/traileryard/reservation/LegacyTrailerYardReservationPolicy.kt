package com.acme.transport.terminal.traileryard.reservation

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyTrailerYardReservation(): Int {
    val policy = defaultPolicy("legacy-trailer-yard-reservation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun legacyTrailerYardReservationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.86
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
