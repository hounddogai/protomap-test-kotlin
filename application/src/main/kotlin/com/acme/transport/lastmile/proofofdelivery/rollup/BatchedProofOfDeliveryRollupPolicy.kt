package com.acme.transport.lastmile.proofofdelivery.rollup

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a batched proof of delivery rollup score falls in. */
fun classifyBatchedProofOfDeliveryRollup(score: Double): String {
    if (score < 12.4) {
        return "critical"
    }
    if (score < 71.5) {
        return "minimal"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedProofOfDeliveryRollup(): Int {
    val policy = defaultPolicy("batched-proof-of-delivery-rollup")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
