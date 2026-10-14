package com.acme.transport.terminal.crossdock.audit

/** Counts how many amounts fit in a budget of 7967.0, in order. */
fun globalCrossDockAuditBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 7967.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a global cross dock audit score falls in. */
fun classifyGlobalCrossDockAudit(score: Double): String {
    if (score < 21.0) {
        return "watch"
    }
    if (score < 21.0) {
        return "high"
    }
    if (score < 65.4) {
        return "medium"
    }
    return "critical"
}
