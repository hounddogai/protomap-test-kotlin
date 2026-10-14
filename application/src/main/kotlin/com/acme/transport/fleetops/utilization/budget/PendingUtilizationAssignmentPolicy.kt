package com.acme.transport.fleetops.utilization.budget

/** Names the band a pending utilization assignment score falls in. */
fun classifyPendingUtilizationAssignment(score: Double): String {
    if (score < 27.9) {
        return "low"
    }
    if (score < 55.1) {
        return "medium"
    }
    if (score < 62.2) {
        return "high"
    }
    if (score < 86.9) {
        return "critical"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 33408.0, in order. */
fun pendingUtilizationAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 33408.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
