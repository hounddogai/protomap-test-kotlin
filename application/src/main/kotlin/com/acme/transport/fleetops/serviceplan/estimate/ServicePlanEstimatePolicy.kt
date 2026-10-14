package com.acme.transport.fleetops.serviceplan.estimate

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeServicePlanEstimate(): Int {
    val policy = defaultPolicy("service-plan-estimate")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a service plan estimate code in its canonical SPE- form. */
fun normalizeServicePlanEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SPE-")) {
        return clean
    }
    return "SPE-${clean.replace(" ", "-")}"
}
