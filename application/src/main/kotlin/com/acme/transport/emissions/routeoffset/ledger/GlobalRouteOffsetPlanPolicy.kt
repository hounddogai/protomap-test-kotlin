package com.acme.transport.emissions.routeoffset.ledger

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalRouteOffsetPlan(): Int {
    val policy = defaultPolicy("global-route-offset-plan")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun globalRouteOffsetPlanSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Puts a global route offset plan code in its canonical GROP- form. */
fun normalizeGlobalRouteOffsetPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GROP-")) {
        return clean
    }
    return "GROP-${clean.replace(" ", "-")}"
}

/** Names the band a global route offset plan score falls in. */
fun classifyGlobalRouteOffsetPlan(score: Double): String {
    if (score < 2.3) {
        return "minimal"
    }
    if (score < 95.2) {
        return "high"
    }
    return "critical"
}
