package com.acme.transport.lastmile.proofofdelivery.baseline

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun weightedProofOfDeliveryThresholdBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedProofOfDeliveryThreshold(): Int {
    val policy = defaultPolicy("weighted-proof-of-delivery-threshold")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
