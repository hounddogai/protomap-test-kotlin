package com.acme.transport.fleetops.utilization.tieredutilizationplan

/** Counts how many amounts fit in a budget of 37971.0, in order. */
fun legacyUtilizationSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 37971.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a legacy utilization segment score falls in. */
fun classifyLegacyUtilizationSegment(score: Double): String {
    if (score < 53.4) {
        return "elevated"
    }
    if (score < 91.1) {
        return "watch"
    }
    return "minimal"
}
