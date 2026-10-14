package com.acme.transport.crewing.availability.manifest

/** Counts how many amounts fit in a budget of 94727.0, in order. */
fun manualAvailabilityRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 94727.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a manual availability request score falls in. */
fun classifyManualAvailabilityRequest(score: Double): String {
    if (score < 3.9) {
        return "minimal"
    }
    if (score < 12.5) {
        return "critical"
    }
    if (score < 38.1) {
        return "watch"
    }
    if (score < 95.2) {
        return "high"
    }
    return "elevated"
}
