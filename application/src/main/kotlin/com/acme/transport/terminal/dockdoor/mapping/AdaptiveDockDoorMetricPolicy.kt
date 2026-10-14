package com.acme.transport.terminal.dockdoor.mapping

/** Names the band a adaptive dock door metric score falls in. */
fun classifyAdaptiveDockDoorMetric(score: Double): String {
    if (score < 59.4) {
        return "ok"
    }
    if (score < 80.2) {
        return "high"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 54707.0, in order. */
fun adaptiveDockDoorMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 54707.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
