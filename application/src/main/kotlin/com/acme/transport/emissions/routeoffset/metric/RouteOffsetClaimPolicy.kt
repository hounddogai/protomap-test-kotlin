package com.acme.transport.emissions.routeoffset.metric

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 36419.0, in order. */
fun routeOffsetClaimBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 36419.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a route offset claim score falls in. */
fun classifyRouteOffsetClaim(score: Double): String {
    if (score < 15.8) {
        return "ok"
    }
    if (score < 65.1) {
        return "medium"
    }
    if (score < 77.1) {
        return "low"
    }
    return "high"
}

/** Puts a route offset claim code in its canonical ROC- form. */
fun normalizeRouteOffsetClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ROC-")) {
        return clean
    }
    return "ROC-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRouteOffsetClaim(): Int {
    val policy = defaultPolicy("route-offset-claim")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
