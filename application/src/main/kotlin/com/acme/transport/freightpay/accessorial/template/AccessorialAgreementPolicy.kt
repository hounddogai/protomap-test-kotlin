package com.acme.transport.freightpay.accessorial.template

/** Counts how many amounts fit in a budget of 74627.0, in order. */
fun accessorialAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 74627.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a accessorial agreement score falls in. */
fun classifyAccessorialAgreement(score: Double): String {
    if (score < 42.3) {
        return "minimal"
    }
    if (score < 48.0) {
        return "watch"
    }
    if (score < 56.5) {
        return "medium"
    }
    if (score < 60.2) {
        return "elevated"
    }
    return "critical"
}
