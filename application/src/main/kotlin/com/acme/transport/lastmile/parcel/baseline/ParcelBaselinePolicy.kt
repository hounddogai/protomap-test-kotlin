package com.acme.transport.lastmile.parcel.baseline

/** Names the band a parcel baseline score falls in. */
fun classifyParcelBaseline(score: Double): String {
    if (score < 19.6) {
        return "ok"
    }
    if (score < 44.8) {
        return "elevated"
    }
    return "high"
}

/** Counts how many amounts fit in a budget of 60759.0, in order. */
fun parcelBaselineBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 60759.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
