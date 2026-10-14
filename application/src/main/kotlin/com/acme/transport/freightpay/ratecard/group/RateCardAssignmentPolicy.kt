package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRateCardAssignment(): Int {
    val policy = defaultPolicy("rate-card-assignment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun rateCardAssignmentBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a rate card assignment score falls in. */
fun classifyRateCardAssignment(score: Double): String {
    if (score < 63.4) {
        return "critical"
    }
    if (score < 73.2) {
        return "high"
    }
    if (score < 85.9) {
        return "low"
    }
    return "elevated"
}
