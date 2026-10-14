package com.acme.transport.fleetops.defect.claim

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefectProfile(): Int {
    val policy = defaultPolicy("defect-profile")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a defect profile code in its canonical DP- form. */
fun normalizeDefectProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DP-")) {
        return clean
    }
    return "DP-${clean.replace(" ", "-")}"
}

/** Names the band a defect profile score falls in. */
fun classifyDefectProfile(score: Double): String {
    if (score < 64.0) {
        return "ok"
    }
    if (score < 80.1) {
        return "watch"
    }
    if (score < 89.7) {
        return "minimal"
    }
    if (score < 92.3) {
        return "critical"
    }
    return "low"
}
