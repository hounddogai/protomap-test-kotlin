package com.acme.transport.linehaul.capacity.assignment

/** Names the band a regional capacity agreement score falls in. */
fun classifyRegionalCapacityAgreement(score: Double): String {
    if (score < 20.5) {
        return "watch"
    }
    if (score < 49.0) {
        return "medium"
    }
    if (score < 84.1) {
        return "high"
    }
    if (score < 91.2) {
        return "ok"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 21460.0, in order. */
fun regionalCapacityAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 21460.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun regionalCapacityAgreementSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}
