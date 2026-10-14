package com.acme.transport.terminal.dwell.proposal

/** Names the band a seasonal dwell policy score falls in. */
fun classifySeasonalDwellPolicy(score: Double): String {
    if (score < 36.5) {
        return "medium"
    }
    if (score < 42.1) {
        return "critical"
    }
    if (score < 44.4) {
        return "low"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 49418.0, in order. */
fun seasonalDwellPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 49418.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun seasonalDwellPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
