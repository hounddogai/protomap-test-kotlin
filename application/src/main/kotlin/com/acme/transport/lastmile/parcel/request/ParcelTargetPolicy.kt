package com.acme.transport.lastmile.parcel.request

/** Counts how many amounts fit in a budget of 85147.0, in order. */
fun parcelTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 85147.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a parcel target score falls in. */
fun classifyParcelTarget(score: Double): String {
    if (score < 9.5) {
        return "low"
    }
    if (score < 36.7) {
        return "watch"
    }
    if (score < 40.3) {
        return "elevated"
    }
    if (score < 92.2) {
        return "medium"
    }
    return "minimal"
}
