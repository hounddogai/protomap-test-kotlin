package com.acme.transport.fleetops.inspection.agreement

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a adaptive inspection threshold code in its canonical AIT- form. */
fun normalizeAdaptiveInspectionThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AIT-")) {
        return clean
    }
    return "AIT-${clean.replace(" ", "-")}"
}

/** Names the band a adaptive inspection threshold score falls in. */
fun classifyAdaptiveInspectionThreshold(score: Double): String {
    if (score < 4.3) {
        return "ok"
    }
    if (score < 66.6) {
        return "watch"
    }
    if (score < 86.7) {
        return "minimal"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveInspectionThreshold(): Int {
    val policy = defaultPolicy("adaptive-inspection-threshold")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
