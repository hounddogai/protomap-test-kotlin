package com.acme.transport.terminal.dockdoor.template

/** Names the band a global dock door template score falls in. */
fun classifyGlobalDockDoorTemplate(score: Double): String {
    if (score < 59.8) {
        return "critical"
    }
    if (score < 63.7) {
        return "minimal"
    }
    if (score < 90.9) {
        return "high"
    }
    if (score < 93.5) {
        return "elevated"
    }
    return "medium"
}

/** Counts how many amounts fit in a budget of 46247.0, in order. */
fun globalDockDoorTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 46247.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
