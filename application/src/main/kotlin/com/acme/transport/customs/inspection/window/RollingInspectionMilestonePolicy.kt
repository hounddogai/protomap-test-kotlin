package com.acme.transport.customs.inspection.window

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingInspectionMilestone(): Int {
    val policy = defaultPolicy("rolling-inspection-milestone")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a rolling inspection milestone score falls in. */
fun classifyRollingInspectionMilestone(score: Double): String {
    if (score < 18.4) {
        return "low"
    }
    if (score < 36.6) {
        return "medium"
    }
    if (score < 63.6) {
        return "high"
    }
    return "watch"
}

/** Puts a rolling inspection milestone code in its canonical RIM- form. */
fun normalizeRollingInspectionMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RIM-")) {
        return clean
    }
    return "RIM-${clean.replace(" ", "-")}"
}
