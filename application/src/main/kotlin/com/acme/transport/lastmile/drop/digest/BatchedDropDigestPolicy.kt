package com.acme.transport.lastmile.drop.digest

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedDropDigest(): Int {
    val policy = defaultPolicy("batched-drop-digest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a batched drop digest score falls in. */
fun classifyBatchedDropDigest(score: Double): String {
    if (score < 11.9) {
        return "watch"
    }
    if (score < 51.9) {
        return "medium"
    }
    if (score < 91.8) {
        return "critical"
    }
    if (score < 97.9) {
        return "low"
    }
    return "minimal"
}
