package com.acme.transport.fleetops.serviceplan.planledger

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a default service plan ledger code in its canonical DSPL- form. */
fun normalizeDefaultServicePlanLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DSPL-")) {
        return clean
    }
    return "DSPL-${clean.replace(" ", "-")}"
}

/** Names the band a default service plan ledger score falls in. */
fun classifyDefaultServicePlanLedger(score: Double): String {
    if (score < 2.5) {
        return "high"
    }
    if (score < 20.4) {
        return "ok"
    }
    return "critical"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultServicePlanLedger(): Int {
    val policy = defaultPolicy("default-service-plan-ledger")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
