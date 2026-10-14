package com.acme.transport.fleetops.leasereturn.template

import com.acme.transport.kit.retry.defaultPolicy

fun strictLeaseReturnReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictLeaseReturnReservation(): Int {
    val policy = defaultPolicy("strict-lease-return-reservation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a strict lease return reservation score falls in. */
fun classifyStrictLeaseReturnReservation(score: Double): String {
    if (score < 24.2) {
        return "minimal"
    }
    if (score < 37.2) {
        return "medium"
    }
    if (score < 58.5) {
        return "high"
    }
    if (score < 80.5) {
        return "watch"
    }
    return "low"
}
