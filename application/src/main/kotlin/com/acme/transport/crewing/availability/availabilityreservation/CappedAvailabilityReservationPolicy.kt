package com.acme.transport.crewing.availability.availabilityreservation

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a capped availability reservation score falls in. */
fun classifyCappedAvailabilityReservation(score: Double): String {
    if (score < 9.5) {
        return "watch"
    }
    if (score < 45.8) {
        return "high"
    }
    if (score < 45.9) {
        return "critical"
    }
    if (score < 49.7) {
        return "ok"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedAvailabilityReservation(): Int {
    val policy = defaultPolicy("capped-availability-reservation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a capped availability reservation code in its canonical CAR- form. */
fun normalizeCappedAvailabilityReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CAR-")) {
        return clean
    }
    return "CAR-${clean.replace(" ", "-")}"
}
