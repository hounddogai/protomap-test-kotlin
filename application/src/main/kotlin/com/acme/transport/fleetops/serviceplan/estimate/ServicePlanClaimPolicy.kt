package com.acme.transport.fleetops.serviceplan.estimate

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a service plan claim code in its canonical SPC- form. */
fun normalizeServicePlanClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SPC-")) {
        return clean
    }
    return "SPC-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeServicePlanClaim(): Int {
    val policy = defaultPolicy("service-plan-claim")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun servicePlanClaimSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 54367.0, in order. */
fun servicePlanClaimBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 54367.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
