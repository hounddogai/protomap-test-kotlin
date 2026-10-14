package com.acme.transport.fleetops.defect.notice

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a batched defect digest score falls in. */
fun classifyBatchedDefectDigest(score: Double): String {
    if (score < 19.2) {
        return "watch"
    }
    if (score < 86.9) {
        return "high"
    }
    if (score < 87.2) {
        return "elevated"
    }
    if (score < 97.2) {
        return "medium"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 9403.0, in order. */
fun batchedDefectDigestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 9403.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedDefectDigest(): Int {
    val policy = defaultPolicy("batched-defect-digest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
