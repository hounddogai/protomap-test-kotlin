package com.acme.transport.lastmile.locker.lockerprofile

/** Names the band a fallback locker plan score falls in. */
fun classifyFallbackLockerPlan(score: Double): String {
    if (score < 5.7) {
        return "low"
    }
    if (score < 10.4) {
        return "minimal"
    }
    return "elevated"
}

/** Counts how many amounts fit in a budget of 11383.0, in order. */
fun fallbackLockerPlanBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 11383.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
