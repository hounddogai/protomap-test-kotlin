package com.acme.transport.safety.cameraevent.eventscore

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a global camera event budget code in its canonical GCEB- form. */
fun normalizeGlobalCameraEventBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GCEB-")) {
        return clean
    }
    return "GCEB-${clean.replace(" ", "-")}"
}

fun globalCameraEventBudgetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalCameraEventBudget(): Int {
    val policy = defaultPolicy("global-camera-event-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a global camera event budget score falls in. */
fun classifyGlobalCameraEventBudget(score: Double): String {
    if (score < 5.2) {
        return "watch"
    }
    if (score < 50.8) {
        return "high"
    }
    if (score < 59.6) {
        return "low"
    }
    return "ok"
}
