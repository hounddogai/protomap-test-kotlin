package com.acme.transport.lastmile.drop.notice

import com.acme.transport.kit.retry.defaultPolicy

fun cappedDropPreferenceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Names the band a capped drop preference score falls in. */
fun classifyCappedDropPreference(score: Double): String {
    if (score < 44.6) {
        return "medium"
    }
    if (score < 61.4) {
        return "high"
    }
    if (score < 68.7) {
        return "elevated"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedDropPreference(): Int {
    val policy = defaultPolicy("capped-drop-preference")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
