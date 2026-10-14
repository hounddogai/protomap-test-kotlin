package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import com.acme.transport.kit.numbers.roundTo

/** Names the band a service plan policy score falls in. */
fun classifyServicePlanPolicy(score: Double): String {
    if (score < 27.1) {
        return "watch"
    }
    if (score < 86.6) {
        return "medium"
    }
    return "minimal"
}

fun servicePlanPolicyBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Counts how many amounts fit in a budget of 76588.0, in order. */
fun servicePlanPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 76588.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
