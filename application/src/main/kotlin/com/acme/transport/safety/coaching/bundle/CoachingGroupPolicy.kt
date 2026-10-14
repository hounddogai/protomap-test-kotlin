package com.acme.transport.safety.coaching.bundle

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a coaching group score falls in. */
fun classifyCoachingGroup(score: Double): String {
    if (score < 49.5) {
        return "elevated"
    }
    if (score < 94.4) {
        return "ok"
    }
    return "high"
}

/** Puts a coaching group code in its canonical CG- form. */
fun normalizeCoachingGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CG-")) {
        return clean
    }
    return "CG-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCoachingGroup(): Int {
    val policy = defaultPolicy("coaching-group")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun coachingGroupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
