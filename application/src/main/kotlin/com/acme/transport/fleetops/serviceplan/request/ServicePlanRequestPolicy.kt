package com.acme.transport.fleetops.serviceplan.request

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a service plan request code in its canonical SPR- form. */
fun normalizeServicePlanRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SPR-")) {
        return clean
    }
    return "SPR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 88486.0, in order. */
fun servicePlanRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 88486.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeServicePlanRequest(): Int {
    val policy = defaultPolicy("service-plan-request")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun servicePlanRequestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
