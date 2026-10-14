package com.acme.transport.customs.inspection.digest

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 2949.0, in order. */
fun inspectionDigestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 2949.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun inspectionDigestBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeInspectionDigest(): Int {
    val policy = defaultPolicy("inspection-digest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a inspection digest score falls in. */
fun classifyInspectionDigest(score: Double): String {
    if (score < 41.1) {
        return "low"
    }
    if (score < 41.4) {
        return "medium"
    }
    return "elevated"
}
