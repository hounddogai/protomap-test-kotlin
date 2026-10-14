package com.acme.transport.terminal.dwell.rule

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedDwellRule(): Int {
    val policy = defaultPolicy("capped-dwell-rule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a capped dwell rule score falls in. */
fun classifyCappedDwellRule(score: Double): String {
    if (score < 29.7) {
        return "medium"
    }
    if (score < 63.1) {
        return "elevated"
    }
    if (score < 83.8) {
        return "critical"
    }
    return "watch"
}
