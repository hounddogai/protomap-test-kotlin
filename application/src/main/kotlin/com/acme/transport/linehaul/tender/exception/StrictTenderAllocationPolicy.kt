package com.acme.transport.linehaul.tender.exception

/** Names the band a strict tender allocation score falls in. */
fun classifyStrictTenderAllocation(score: Double): String {
    if (score < 17.0) {
        return "elevated"
    }
    if (score < 54.4) {
        return "medium"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 95998.0, in order. */
fun strictTenderAllocationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 95998.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
