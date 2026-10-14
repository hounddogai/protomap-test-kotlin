package com.acme.transport.lastmile.redelivery.milestone

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a redelivery reservation score falls in. */
fun classifyRedeliveryReservation(score: Double): String {
    if (score < 10.4) {
        return "medium"
    }
    if (score < 52.5) {
        return "ok"
    }
    if (score < 98.7) {
        return "elevated"
    }
    return "critical"
}

fun redeliveryReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRedeliveryReservation(): Int {
    val policy = defaultPolicy("redelivery-reservation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a redelivery reservation code in its canonical RR- form. */
fun normalizeRedeliveryReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RR-")) {
        return clean
    }
    return "RR-${clean.replace(" ", "-")}"
}
