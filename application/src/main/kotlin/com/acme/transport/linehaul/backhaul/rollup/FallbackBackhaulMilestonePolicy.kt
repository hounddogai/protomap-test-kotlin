package com.acme.transport.linehaul.backhaul.rollup

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackBackhaulMilestone(): Int {
    val policy = defaultPolicy("fallback-backhaul-milestone")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a fallback backhaul milestone score falls in. */
fun classifyFallbackBackhaulMilestone(score: Double): String {
    if (score < 9.2) {
        return "high"
    }
    if (score < 41.5) {
        return "minimal"
    }
    if (score < 90.3) {
        return "low"
    }
    return "ok"
}
