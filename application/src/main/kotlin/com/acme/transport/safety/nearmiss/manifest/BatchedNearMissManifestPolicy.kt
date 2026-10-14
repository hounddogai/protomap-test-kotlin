package com.acme.transport.safety.nearmiss.manifest

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedNearMissManifest(): Int {
    val policy = defaultPolicy("batched-near-miss-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 88986.0, in order. */
fun batchedNearMissManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 88986.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun batchedNearMissManifestBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
