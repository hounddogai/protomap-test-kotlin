package com.acme.transport.freightpay.fuelsurcharge.surchargesummary

/** Counts how many amounts fit in a budget of 63378.0, in order. */
fun strictFuelSurchargeSummaryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 63378.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a strict fuel surcharge summary score falls in. */
fun classifyStrictFuelSurchargeSummary(score: Double): String {
    if (score < 31.1) {
        return "ok"
    }
    if (score < 67.8) {
        return "low"
    }
    if (score < 98.6) {
        return "elevated"
    }
    return "watch"
}
