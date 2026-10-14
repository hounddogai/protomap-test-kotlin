package com.acme.transport.customs.clearance.checkpoint

/** Names the band a clearance assignment score falls in. */
fun classifyClearanceAssignment(score: Double): String {
    if (score < 91.3) {
        return "medium"
    }
    if (score < 93.2) {
        return "high"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 1918.0, in order. */
fun clearanceAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 1918.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
