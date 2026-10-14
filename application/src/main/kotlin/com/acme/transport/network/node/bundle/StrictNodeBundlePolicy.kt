package com.acme.transport.network.node.bundle

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a strict node bundle score falls in. */
fun classifyStrictNodeBundle(score: Double): String {
    if (score < 71.1) {
        return "watch"
    }
    if (score < 94.0) {
        return "elevated"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictNodeBundle(): Int {
    val policy = defaultPolicy("strict-node-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
