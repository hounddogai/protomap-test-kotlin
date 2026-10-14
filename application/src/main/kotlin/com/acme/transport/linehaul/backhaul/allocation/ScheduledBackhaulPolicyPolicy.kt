package com.acme.transport.linehaul.backhaul.allocation

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a scheduled backhaul policy score falls in. */
fun classifyScheduledBackhaulPolicy(score: Double): String {
    if (score < 4.4) {
        return "minimal"
    }
    if (score < 6.8) {
        return "elevated"
    }
    if (score < 11.9) {
        return "low"
    }
    if (score < 46.8) {
        return "critical"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledBackhaulPolicy(): Int {
    val policy = defaultPolicy("scheduled-backhaul-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun scheduledBackhaulPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
