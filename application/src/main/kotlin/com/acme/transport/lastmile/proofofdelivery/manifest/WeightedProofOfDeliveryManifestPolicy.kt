package com.acme.transport.lastmile.proofofdelivery.manifest

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun weightedProofOfDeliveryManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedProofOfDeliveryManifest(): Int {
    val policy = defaultPolicy("weighted-proof-of-delivery-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 45617.0, in order. */
fun weightedProofOfDeliveryManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 45617.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun weightedProofOfDeliveryManifestBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
