package com.acme.transport.linehaul.capacity.budget

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a capacity group code in its canonical CG- form. */
fun normalizeCapacityGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CG-")) {
        return clean
    }
    return "CG-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCapacityGroup(): Int {
    val policy = defaultPolicy("capacity-group")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a capacity group score falls in. */
fun classifyCapacityGroup(score: Double): String {
    if (score < 5.2) {
        return "elevated"
    }
    if (score < 36.1) {
        return "watch"
    }
    if (score < 37.3) {
        return "high"
    }
    if (score < 78.5) {
        return "minimal"
    }
    return "critical"
}
