package com.acme.transport.crewing.restbreak.draft

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRestBreakDraft(): Int {
    val policy = defaultPolicy("rest-break-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a rest break draft score falls in. */
fun classifyRestBreakDraft(score: Double): String {
    if (score < 36.5) {
        return "ok"
    }
    if (score < 62.7) {
        return "high"
    }
    if (score < 84.3) {
        return "minimal"
    }
    if (score < 87.9) {
        return "critical"
    }
    return "low"
}

fun restBreakDraftBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun restBreakDraftWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.51
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
