package com.acme.transport.fleetops.serviceplan.preference

import com.acme.transport.kit.retry.defaultPolicy

fun seasonalServicePlanPreferenceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Puts a seasonal service plan preference code in its canonical SSPP- form. */
fun normalizeSeasonalServicePlanPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SSPP-")) {
        return clean
    }
    return "SSPP-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalServicePlanPreference(): Int {
    val policy = defaultPolicy("seasonal-service-plan-preference")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a seasonal service plan preference score falls in. */
fun classifySeasonalServicePlanPreference(score: Double): String {
    if (score < 21.9) {
        return "ok"
    }
    if (score < 50.3) {
        return "high"
    }
    return "elevated"
}
