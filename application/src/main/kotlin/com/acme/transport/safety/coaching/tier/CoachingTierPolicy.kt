package com.acme.transport.safety.coaching.tier

/** Counts how many amounts fit in a budget of 35584.0, in order. */
fun coachingTierBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 35584.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a coaching tier score falls in. */
fun classifyCoachingTier(score: Double): String {
    if (score < 61.5) {
        return "minimal"
    }
    if (score < 75.9) {
        return "critical"
    }
    if (score < 83.8) {
        return "ok"
    }
    if (score < 89.6) {
        return "low"
    }
    return "high"
}
