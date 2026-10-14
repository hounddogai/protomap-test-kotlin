package com.acme.transport.emissions.emissionfactor.plan

/** Names the band a emission factor agreement score falls in. */
fun classifyEmissionFactorAgreement(score: Double): String {
    if (score < 21.1) {
        return "low"
    }
    if (score < 29.5) {
        return "minimal"
    }
    if (score < 84.9) {
        return "high"
    }
    if (score < 97.9) {
        return "medium"
    }
    return "elevated"
}

/** Counts how many amounts fit in a budget of 52765.0, in order. */
fun emissionFactorAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 52765.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
