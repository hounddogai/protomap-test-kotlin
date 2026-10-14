package com.acme.transport.linehaul.capacity.summary

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a capacity summary score falls in. */
fun classifyCapacitySummary(score: Double): String {
    if (score < 16.4) {
        return "critical"
    }
    if (score < 29.4) {
        return "medium"
    }
    if (score < 39.4) {
        return "watch"
    }
    return "ok"
}

fun capacitySummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCapacitySummary(): Int {
    val policy = defaultPolicy("capacity-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a capacity summary code in its canonical CS- form. */
fun normalizeCapacitySummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CS-")) {
        return clean
    }
    return "CS-${clean.replace(" ", "-")}"
}
