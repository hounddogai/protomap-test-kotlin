package com.acme.transport.terminal.traileryard.score

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a trailer yard audit score falls in. */
fun classifyTrailerYardAudit(score: Double): String {
    if (score < 20.7) {
        return "elevated"
    }
    if (score < 32.7) {
        return "minimal"
    }
    if (score < 84.9) {
        return "low"
    }
    return "ok"
}

fun trailerYardAuditWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.8
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTrailerYardAudit(): Int {
    val policy = defaultPolicy("trailer-yard-audit")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun trailerYardAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}
