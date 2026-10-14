package com.acme.transport.lastmile.drop.forecast

/** Counts how many amounts fit in a budget of 97692.0, in order. */
fun defaultDropAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 97692.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun defaultDropAgreementSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Names the band a default drop agreement score falls in. */
fun classifyDefaultDropAgreement(score: Double): String {
    if (score < 6.1) {
        return "elevated"
    }
    if (score < 38.4) {
        return "watch"
    }
    return "low"
}
