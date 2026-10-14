package com.acme.transport.terminal.dockdoor.exception

/** Names the band a primary dock door summary score falls in. */
fun classifyPrimaryDockDoorSummary(score: Double): String {
    if (score < 14.4) {
        return "minimal"
    }
    if (score < 52.6) {
        return "medium"
    }
    if (score < 76.7) {
        return "critical"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 34968.0, in order. */
fun primaryDockDoorSummaryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 34968.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
