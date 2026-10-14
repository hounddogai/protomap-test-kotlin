package com.acme.transport.crewing.roster.rostermilestone

/** Counts how many amounts fit in a budget of 63409.0, in order. */
fun strictRosterSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 63409.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a strict roster segment score falls in. */
fun classifyStrictRosterSegment(score: Double): String {
    if (score < 17.9) {
        return "high"
    }
    if (score < 28.0) {
        return "minimal"
    }
    if (score < 41.2) {
        return "ok"
    }
    return "critical"
}
