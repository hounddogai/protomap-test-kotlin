package com.acme.transport.terminal.dockdoor.recorditem

/** Names the band a adaptive dock door cycle score falls in. */
fun classifyAdaptiveDockDoorCycle(score: Double): String {
    if (score < 19.7) {
        return "low"
    }
    if (score < 81.2) {
        return "watch"
    }
    if (score < 91.0) {
        return "critical"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 99570.0, in order. */
fun adaptiveDockDoorCycleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 99570.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
