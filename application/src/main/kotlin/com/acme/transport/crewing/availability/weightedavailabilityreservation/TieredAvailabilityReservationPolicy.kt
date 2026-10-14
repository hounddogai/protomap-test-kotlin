package com.acme.transport.crewing.availability.weightedavailabilityreservation

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredAvailabilityReservation(): Int {
    val policy = defaultPolicy("tiered-availability-reservation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun tieredAvailabilityReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Puts a tiered availability reservation code in its canonical TAR- form. */
fun normalizeTieredAvailabilityReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TAR-")) {
        return clean
    }
    return "TAR-${clean.replace(" ", "-")}"
}
