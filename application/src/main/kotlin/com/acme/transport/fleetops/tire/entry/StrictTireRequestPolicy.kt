package com.acme.transport.fleetops.tire.entry

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a strict tire request code in its canonical STR- form. */
fun normalizeStrictTireRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("STR-")) {
        return clean
    }
    return "STR-${clean.replace(" ", "-")}"
}

/** Names the band a strict tire request score falls in. */
fun classifyStrictTireRequest(score: Double): String {
    if (score < 24.4) {
        return "minimal"
    }
    if (score < 42.8) {
        return "ok"
    }
    if (score < 90.0) {
        return "watch"
    }
    if (score < 96.3) {
        return "low"
    }
    return "critical"
}

fun strictTireRequestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictTireRequest(): Int {
    val policy = defaultPolicy("strict-tire-request")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
