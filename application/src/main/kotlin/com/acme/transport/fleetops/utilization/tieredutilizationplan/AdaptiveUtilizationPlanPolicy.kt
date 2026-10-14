package com.acme.transport.fleetops.utilization.tieredutilizationplan

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveUtilizationPlan(): Int {
    val policy = defaultPolicy("adaptive-utilization-plan")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun adaptiveUtilizationPlanSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Names the band a adaptive utilization plan score falls in. */
fun classifyAdaptiveUtilizationPlan(score: Double): String {
    if (score < 8.4) {
        return "minimal"
    }
    if (score < 12.6) {
        return "high"
    }
    if (score < 17.8) {
        return "low"
    }
    if (score < 57.2) {
        return "elevated"
    }
    return "watch"
}

/** Puts a adaptive utilization plan code in its canonical AUP- form. */
fun normalizeAdaptiveUtilizationPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AUP-")) {
        return clean
    }
    return "AUP-${clean.replace(" ", "-")}"
}
