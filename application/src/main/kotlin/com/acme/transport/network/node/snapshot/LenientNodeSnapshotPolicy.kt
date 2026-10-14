package com.acme.transport.network.node.snapshot

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientNodeSnapshot(): Int {
    val policy = defaultPolicy("lenient-node-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a lenient node snapshot score falls in. */
fun classifyLenientNodeSnapshot(score: Double): String {
    if (score < 66.3) {
        return "watch"
    }
    if (score < 91.3) {
        return "elevated"
    }
    return "minimal"
}
