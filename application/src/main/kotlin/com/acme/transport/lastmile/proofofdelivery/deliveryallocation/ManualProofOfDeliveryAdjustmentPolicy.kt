package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

/** Names the band a manual proof of delivery adjustment score falls in. */
fun classifyManualProofOfDeliveryAdjustment(score: Double): String {
    if (score < 16.3) {
        return "ok"
    }
    if (score < 27.6) {
        return "low"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 54673.0, in order. */
fun manualProofOfDeliveryAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 54673.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
