package com.acme.transport.customs.broker.reservation

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun brokerReservationBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun brokerReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Names the band a broker reservation score falls in. */
fun classifyBrokerReservation(score: Double): String {
    if (score < 24.8) {
        return "ok"
    }
    if (score < 67.0) {
        return "critical"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBrokerReservation(): Int {
    val policy = defaultPolicy("broker-reservation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
