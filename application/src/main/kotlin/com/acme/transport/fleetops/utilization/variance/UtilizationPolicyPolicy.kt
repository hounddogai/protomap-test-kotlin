package com.acme.transport.fleetops.utilization.variance

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeUtilizationPolicy(): Int {
    val policy = defaultPolicy("utilization-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a utilization policy code in its canonical UP- form. */
fun normalizeUtilizationPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("UP-")) {
        return clean
    }
    return "UP-${clean.replace(" ", "-")}"
}
