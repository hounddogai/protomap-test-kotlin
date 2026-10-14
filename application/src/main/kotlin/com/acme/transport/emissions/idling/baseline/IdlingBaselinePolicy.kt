package com.acme.transport.emissions.idling.baseline

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeIdlingBaseline(): Int {
    val policy = defaultPolicy("idling-baseline")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a idling baseline code in its canonical IB- form. */
fun normalizeIdlingBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IB-")) {
        return clean
    }
    return "IB-${clean.replace(" ", "-")}"
}

fun idlingBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Names the band a idling baseline score falls in. */
fun classifyIdlingBaseline(score: Double): String {
    if (score < 22.6) {
        return "critical"
    }
    if (score < 56.5) {
        return "elevated"
    }
    if (score < 80.1) {
        return "medium"
    }
    return "low"
}
