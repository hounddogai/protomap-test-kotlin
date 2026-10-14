package com.acme.transport.crewing.roster.rostermilestone

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a lenient roster rule score falls in. */
fun classifyLenientRosterRule(score: Double): String {
    if (score < 16.2) {
        return "medium"
    }
    if (score < 52.1) {
        return "elevated"
    }
    if (score < 62.9) {
        return "critical"
    }
    if (score < 68.7) {
        return "high"
    }
    return "minimal"
}

fun lenientRosterRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientRosterRule(): Int {
    val policy = defaultPolicy("lenient-roster-rule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
