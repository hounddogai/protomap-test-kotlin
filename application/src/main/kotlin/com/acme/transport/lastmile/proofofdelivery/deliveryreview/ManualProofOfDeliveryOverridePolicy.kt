package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualProofOfDeliveryOverride(): Int {
    val policy = defaultPolicy("manual-proof-of-delivery-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 58596.0, in order. */
fun manualProofOfDeliveryOverrideBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 58596.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a manual proof of delivery override score falls in. */
fun classifyManualProofOfDeliveryOverride(score: Double): String {
    if (score < 5.1) {
        return "elevated"
    }
    if (score < 32.0) {
        return "watch"
    }
    return "low"
}

fun manualProofOfDeliveryOverrideBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
