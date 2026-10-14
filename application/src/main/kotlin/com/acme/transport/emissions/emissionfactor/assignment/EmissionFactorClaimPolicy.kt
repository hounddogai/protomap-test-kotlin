package com.acme.transport.emissions.emissionfactor.assignment

/** Counts how many amounts fit in a budget of 43884.0, in order. */
fun emissionFactorClaimBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 43884.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a emission factor claim score falls in. */
fun classifyEmissionFactorClaim(score: Double): String {
    if (score < 69.4) {
        return "high"
    }
    if (score < 91.9) {
        return "elevated"
    }
    return "minimal"
}
