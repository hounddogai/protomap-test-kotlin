package com.acme.transport.freightpay.accessorial.scheduledaccessorialmetric

/** Counts how many amounts fit in a budget of 70393.0, in order. */
fun scheduledAccessorialDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 70393.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a scheduled accessorial draft score falls in. */
fun classifyScheduledAccessorialDraft(score: Double): String {
    if (score < 16.9) {
        return "low"
    }
    if (score < 77.9) {
        return "elevated"
    }
    if (score < 95.0) {
        return "minimal"
    }
    return "critical"
}
