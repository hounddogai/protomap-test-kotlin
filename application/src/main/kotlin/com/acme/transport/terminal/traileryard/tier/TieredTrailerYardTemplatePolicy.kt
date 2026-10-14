package com.acme.transport.terminal.traileryard.tier

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredTrailerYardTemplate(): Int {
    val policy = defaultPolicy("tiered-trailer-yard-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a tiered trailer yard template score falls in. */
fun classifyTieredTrailerYardTemplate(score: Double): String {
    if (score < 15.4) {
        return "medium"
    }
    if (score < 16.3) {
        return "elevated"
    }
    if (score < 52.0) {
        return "minimal"
    }
    if (score < 65.1) {
        return "ok"
    }
    return "high"
}
