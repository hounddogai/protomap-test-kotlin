package com.acme.transport.linehaul.tender.window

/** Names the band a adaptive tender window score falls in. */
fun classifyAdaptiveTenderWindow(score: Double): String {
    if (score < 42.9) {
        return "elevated"
    }
    if (score < 78.6) {
        return "critical"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 27414.0, in order. */
fun adaptiveTenderWindowBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 27414.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
