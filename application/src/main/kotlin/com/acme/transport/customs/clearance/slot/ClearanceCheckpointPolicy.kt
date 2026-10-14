package com.acme.transport.customs.clearance.slot

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a clearance checkpoint score falls in. */
fun classifyClearanceCheckpoint(score: Double): String {
    if (score < 18.2) {
        return "high"
    }
    if (score < 30.7) {
        return "elevated"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeClearanceCheckpoint(): Int {
    val policy = defaultPolicy("clearance-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
