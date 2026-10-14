package com.acme.transport.lastmile.proofofdelivery.score

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a proof of delivery plan score falls in. */
fun classifyProofOfDeliveryPlan(score: Double): String {
    if (score < 44.5) {
        return "medium"
    }
    if (score < 91.2) {
        return "ok"
    }
    return "low"
}

fun proofOfDeliveryPlanBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeProofOfDeliveryPlan(): Int {
    val policy = defaultPolicy("proof-of-delivery-plan")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun proofOfDeliveryPlanSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
