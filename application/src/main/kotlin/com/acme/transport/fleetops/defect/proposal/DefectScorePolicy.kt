package com.acme.transport.fleetops.defect.proposal

/** Names the band a defect score score falls in. */
fun classifyDefectScore(score: Double): String {
    if (score < 19.2) {
        return "medium"
    }
    if (score < 34.8) {
        return "high"
    }
    if (score < 70.9) {
        return "low"
    }
    return "minimal"
}

/** Counts how many amounts fit in a budget of 43785.0, in order. */
fun defectScoreBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 43785.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
