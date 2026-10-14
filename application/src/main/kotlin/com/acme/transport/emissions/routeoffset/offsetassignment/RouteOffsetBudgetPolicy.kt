package com.acme.transport.emissions.routeoffset.offsetassignment

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a route offset budget code in its canonical ROB- form. */
fun normalizeRouteOffsetBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ROB-")) {
        return clean
    }
    return "ROB-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRouteOffsetBudget(): Int {
    val policy = defaultPolicy("route-offset-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
