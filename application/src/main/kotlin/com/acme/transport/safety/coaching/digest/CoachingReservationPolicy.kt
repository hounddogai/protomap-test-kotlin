package com.acme.transport.safety.coaching.digest

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun coachingReservationBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a coaching reservation score falls in. */
fun classifyCoachingReservation(score: Double): String {
    if (score < 3.6) {
        return "medium"
    }
    if (score < 89.4) {
        return "ok"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCoachingReservation(): Int {
    val policy = defaultPolicy("coaching-reservation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun coachingReservationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.47
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
