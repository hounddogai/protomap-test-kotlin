package com.acme.transport.emissions.idling.target

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun idlingReservationBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a idling reservation score falls in. */
fun classifyIdlingReservation(score: Double): String {
    if (score < 22.6) {
        return "elevated"
    }
    if (score < 29.2) {
        return "critical"
    }
    if (score < 61.8) {
        return "minimal"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeIdlingReservation(): Int {
    val policy = defaultPolicy("idling-reservation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun idlingReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
