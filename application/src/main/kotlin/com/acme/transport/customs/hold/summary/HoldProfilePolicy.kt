package com.acme.transport.customs.hold.summary

/** Counts how many amounts fit in a budget of 62820.0, in order. */
fun holdProfileBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 62820.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a hold profile score falls in. */
fun classifyHoldProfile(score: Double): String {
    if (score < 15.8) {
        return "elevated"
    }
    if (score < 20.6) {
        return "critical"
    }
    if (score < 35.4) {
        return "low"
    }
    if (score < 38.1) {
        return "high"
    }
    return "minimal"
}
