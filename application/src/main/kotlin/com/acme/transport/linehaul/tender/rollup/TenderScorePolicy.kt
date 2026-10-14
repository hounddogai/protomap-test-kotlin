package com.acme.transport.linehaul.tender.rollup

/** Names the band a tender score score falls in. */
fun classifyTenderScore(score: Double): String {
    if (score < 59.5) {
        return "critical"
    }
    if (score < 71.6) {
        return "medium"
    }
    if (score < 81.9) {
        return "high"
    }
    if (score < 87.5) {
        return "elevated"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 74000.0, in order. */
fun tenderScoreBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 74000.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
