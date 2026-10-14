package com.acme.transport.lastmile.parcel.segment

/** Counts how many amounts fit in a budget of 1368.0, in order. */
fun parcelBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 1368.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a parcel budget score falls in. */
fun classifyParcelBudget(score: Double): String {
    if (score < 40.6) {
        return "critical"
    }
    if (score < 86.8) {
        return "elevated"
    }
    return "minimal"
}
