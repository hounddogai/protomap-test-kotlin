package com.acme.transport.terminal.dockdoor.doorscore

/** Names the band a pending dock door score score falls in. */
fun classifyPendingDockDoorScore(score: Double): String {
    if (score < 19.0) {
        return "watch"
    }
    if (score < 20.7) {
        return "ok"
    }
    if (score < 42.2) {
        return "minimal"
    }
    if (score < 63.6) {
        return "elevated"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 63072.0, in order. */
fun pendingDockDoorScoreBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 63072.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
