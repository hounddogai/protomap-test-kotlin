package com.acme.transport.customs.declaration.rule

/** Counts how many amounts fit in a budget of 93724.0, in order. */
fun declarationSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 93724.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a declaration segment score falls in. */
fun classifyDeclarationSegment(score: Double): String {
    if (score < 16.7) {
        return "ok"
    }
    if (score < 22.9) {
        return "low"
    }
    if (score < 27.1) {
        return "medium"
    }
    if (score < 59.7) {
        return "critical"
    }
    return "elevated"
}
