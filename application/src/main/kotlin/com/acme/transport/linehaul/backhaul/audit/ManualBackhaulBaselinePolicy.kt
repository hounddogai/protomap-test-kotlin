package com.acme.transport.linehaul.backhaul.audit

fun manualBackhaulBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 32554.0, in order. */
fun manualBackhaulBaselineBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 32554.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a manual backhaul baseline score falls in. */
fun classifyManualBackhaulBaseline(score: Double): String {
    if (score < 31.4) {
        return "high"
    }
    if (score < 78.0) {
        return "elevated"
    }
    return "low"
}
