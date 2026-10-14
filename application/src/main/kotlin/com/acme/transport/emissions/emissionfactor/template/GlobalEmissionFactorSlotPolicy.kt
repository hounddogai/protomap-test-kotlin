package com.acme.transport.emissions.emissionfactor.template

/** Names the band a global emission factor slot score falls in. */
fun classifyGlobalEmissionFactorSlot(score: Double): String {
    if (score < 81.4) {
        return "elevated"
    }
    if (score < 96.0) {
        return "low"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 38356.0, in order. */
fun globalEmissionFactorSlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 38356.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
