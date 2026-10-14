package com.acme.transport.safety.coaching.coachingscore

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a rolling coaching score code in its canonical RCS- form. */
fun normalizeRollingCoachingScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCS-")) {
        return clean
    }
    return "RCS-${clean.replace(" ", "-")}"
}

fun rollingCoachingScoreSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Names the band a rolling coaching score score falls in. */
fun classifyRollingCoachingScore(score: Double): String {
    if (score < 4.3) {
        return "ok"
    }
    if (score < 22.0) {
        return "watch"
    }
    if (score < 85.3) {
        return "critical"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingCoachingScore(): Int {
    val policy = defaultPolicy("rolling-coaching-score")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
