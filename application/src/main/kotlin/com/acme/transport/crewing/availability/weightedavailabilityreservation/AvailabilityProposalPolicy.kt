package com.acme.transport.crewing.availability.weightedavailabilityreservation

/** Names the band a availability proposal score falls in. */
fun classifyAvailabilityProposal(score: Double): String {
    if (score < 18.0) {
        return "low"
    }
    if (score < 76.2) {
        return "watch"
    }
    if (score < 79.6) {
        return "minimal"
    }
    if (score < 88.0) {
        return "ok"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 82311.0, in order. */
fun availabilityProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 82311.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
