package com.acme.transport.fleetops.defect.policy

/** Names the band a primary defect claim score falls in. */
fun classifyPrimaryDefectClaim(score: Double): String {
    if (score < 36.8) {
        return "medium"
    }
    if (score < 53.8) {
        return "critical"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 43657.0, in order. */
fun primaryDefectClaimBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 43657.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
