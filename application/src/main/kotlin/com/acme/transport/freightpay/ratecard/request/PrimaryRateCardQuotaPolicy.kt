package com.acme.transport.freightpay.ratecard.request

/** Counts how many amounts fit in a budget of 66905.0, in order. */
fun primaryRateCardQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 66905.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a primary rate card quota score falls in. */
fun classifyPrimaryRateCardQuota(score: Double): String {
    if (score < 68.4) {
        return "ok"
    }
    if (score < 83.4) {
        return "elevated"
    }
    if (score < 83.5) {
        return "watch"
    }
    if (score < 84.9) {
        return "low"
    }
    return "minimal"
}
