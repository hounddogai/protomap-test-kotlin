package com.acme.transport.customs.declaration.notice

/** Names the band a scheduled declaration baseline score falls in. */
fun classifyScheduledDeclarationBaseline(score: Double): String {
    if (score < 2.4) {
        return "minimal"
    }
    if (score < 22.8) {
        return "medium"
    }
    if (score < 99.3) {
        return "high"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 99363.0, in order. */
fun scheduledDeclarationBaselineBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 99363.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
