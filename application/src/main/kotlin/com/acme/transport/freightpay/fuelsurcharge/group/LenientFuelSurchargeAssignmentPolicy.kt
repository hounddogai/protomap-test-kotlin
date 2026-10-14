package com.acme.transport.freightpay.fuelsurcharge.group

/** Names the band a lenient fuel surcharge assignment score falls in. */
fun classifyLenientFuelSurchargeAssignment(score: Double): String {
    if (score < 19.6) {
        return "low"
    }
    if (score < 40.4) {
        return "minimal"
    }
    if (score < 46.1) {
        return "high"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 71604.0, in order. */
fun lenientFuelSurchargeAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 71604.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
