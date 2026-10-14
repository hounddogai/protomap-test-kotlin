package com.acme.transport.fleetops.serviceplan.rollup

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeServicePlanDraft(): Int {
    val policy = defaultPolicy("service-plan-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a service plan draft code in its canonical SPD- form. */
fun normalizeServicePlanDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SPD-")) {
        return clean
    }
    return "SPD-${clean.replace(" ", "-")}"
}

/** Names the band a service plan draft score falls in. */
fun classifyServicePlanDraft(score: Double): String {
    if (score < 8.2) {
        return "high"
    }
    if (score < 46.2) {
        return "minimal"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 4448.0, in order. */
fun servicePlanDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 4448.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
