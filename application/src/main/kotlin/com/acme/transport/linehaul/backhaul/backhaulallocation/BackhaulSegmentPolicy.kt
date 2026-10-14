package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a backhaul segment code in its canonical BS- form. */
fun normalizeBackhaulSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BS-")) {
        return clean
    }
    return "BS-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBackhaulSegment(): Int {
    val policy = defaultPolicy("backhaul-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a backhaul segment score falls in. */
fun classifyBackhaulSegment(score: Double): String {
    if (score < 26.1) {
        return "elevated"
    }
    if (score < 47.5) {
        return "minimal"
    }
    if (score < 64.0) {
        return "medium"
    }
    return "low"
}
