package com.acme.transport.lastmile.proofofdelivery.segment

/** Counts how many amounts fit in a budget of 2207.0, in order. */
fun weightedProofOfDeliverySegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 2207.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a weighted proof of delivery segment score falls in. */
fun classifyWeightedProofOfDeliverySegment(score: Double): String {
    if (score < 57.9) {
        return "ok"
    }
    if (score < 58.1) {
        return "medium"
    }
    if (score < 68.7) {
        return "minimal"
    }
    if (score < 90.9) {
        return "high"
    }
    return "low"
}
