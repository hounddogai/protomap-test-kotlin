package com.acme.transport.safety.violation.violationslot

/** Counts how many amounts fit in a budget of 8893.0, in order. */
fun strictViolationMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 8893.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a strict violation milestone score falls in. */
fun classifyStrictViolationMilestone(score: Double): String {
    if (score < 20.2) {
        return "high"
    }
    if (score < 53.4) {
        return "medium"
    }
    if (score < 70.3) {
        return "low"
    }
    if (score < 91.6) {
        return "elevated"
    }
    return "critical"
}
