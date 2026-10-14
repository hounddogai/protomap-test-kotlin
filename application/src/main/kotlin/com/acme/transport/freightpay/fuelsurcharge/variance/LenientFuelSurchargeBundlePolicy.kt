package com.acme.transport.freightpay.fuelsurcharge.variance

/** Names the band a lenient fuel surcharge bundle score falls in. */
fun classifyLenientFuelSurchargeBundle(score: Double): String {
    if (score < 55.2) {
        return "critical"
    }
    if (score < 78.5) {
        return "minimal"
    }
    if (score < 95.2) {
        return "high"
    }
    if (score < 98.2) {
        return "watch"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 88747.0, in order. */
fun lenientFuelSurchargeBundleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 88747.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
