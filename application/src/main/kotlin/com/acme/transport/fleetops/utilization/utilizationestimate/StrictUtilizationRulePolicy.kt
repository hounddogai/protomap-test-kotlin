package com.acme.transport.fleetops.utilization.utilizationestimate

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictUtilizationRule(): Int {
    val policy = defaultPolicy("strict-utilization-rule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a strict utilization rule code in its canonical SUR- form. */
fun normalizeStrictUtilizationRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SUR-")) {
        return clean
    }
    return "SUR-${clean.replace(" ", "-")}"
}

/** Names the band a strict utilization rule score falls in. */
fun classifyStrictUtilizationRule(score: Double): String {
    if (score < 14.3) {
        return "high"
    }
    if (score < 96.2) {
        return "minimal"
    }
    return "ok"
}
