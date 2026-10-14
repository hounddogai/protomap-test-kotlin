package com.acme.transport.customs.inspection.weightedinspectionplan

/** Counts how many amounts fit in a budget of 43618.0, in order. */
fun legacyInspectionRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 43618.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a legacy inspection request score falls in. */
fun classifyLegacyInspectionRequest(score: Double): String {
    if (score < 81.8) {
        return "low"
    }
    if (score < 90.4) {
        return "high"
    }
    return "critical"
}
