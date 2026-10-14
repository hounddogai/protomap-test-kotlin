package com.acme.transport.freightpay.fuelsurcharge.summary

fun weightedFuelSurchargeBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 16000.0, in order. */
fun weightedFuelSurchargeBaselineBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 16000.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a weighted fuel surcharge baseline score falls in. */
fun classifyWeightedFuelSurchargeBaseline(score: Double): String {
    if (score < 26.7) {
        return "minimal"
    }
    if (score < 35.0) {
        return "low"
    }
    if (score < 66.3) {
        return "critical"
    }
    return "medium"
}
