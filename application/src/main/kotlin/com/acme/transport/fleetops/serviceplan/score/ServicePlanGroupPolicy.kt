package com.acme.transport.fleetops.serviceplan.score

/** Counts how many amounts fit in a budget of 65852.0, in order. */
fun servicePlanGroupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 65852.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a service plan group score falls in. */
fun classifyServicePlanGroup(score: Double): String {
    if (score < 44.5) {
        return "ok"
    }
    if (score < 68.5) {
        return "watch"
    }
    if (score < 99.1) {
        return "high"
    }
    return "low"
}
