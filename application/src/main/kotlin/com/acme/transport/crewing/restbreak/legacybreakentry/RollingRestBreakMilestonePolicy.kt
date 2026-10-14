package com.acme.transport.crewing.restbreak.legacybreakentry

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun rollingRestBreakMilestoneBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a rolling rest break milestone score falls in. */
fun classifyRollingRestBreakMilestone(score: Double): String {
    if (score < 33.7) {
        return "ok"
    }
    if (score < 34.5) {
        return "critical"
    }
    if (score < 44.2) {
        return "watch"
    }
    return "low"
}

/** Puts a rolling rest break milestone code in its canonical RRBM- form. */
fun normalizeRollingRestBreakMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RRBM-")) {
        return clean
    }
    return "RRBM-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingRestBreakMilestone(): Int {
    val policy = defaultPolicy("rolling-rest-break-milestone")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
