package com.acme.transport.terminal.traileryard.audit

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a global trailer yard audit score falls in. */
fun classifyGlobalTrailerYardAudit(score: Double): String {
    if (score < 38.5) {
        return "minimal"
    }
    if (score < 64.3) {
        return "elevated"
    }
    return "ok"
}

fun globalTrailerYardAuditBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalTrailerYardAudit(): Int {
    val policy = defaultPolicy("global-trailer-yard-audit")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
