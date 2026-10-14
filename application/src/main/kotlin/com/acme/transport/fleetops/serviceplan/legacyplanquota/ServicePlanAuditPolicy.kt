package com.acme.transport.fleetops.serviceplan.legacyplanquota

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a service plan audit code in its canonical SPA- form. */
fun normalizeServicePlanAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SPA-")) {
        return clean
    }
    return "SPA-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeServicePlanAudit(): Int {
    val policy = defaultPolicy("service-plan-audit")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
