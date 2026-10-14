package com.acme.transport.safety.violation.snapshot

fun cappedViolationThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Counts how many amounts fit in a budget of 11684.0, in order. */
fun cappedViolationThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 11684.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a capped violation threshold score falls in. */
fun classifyCappedViolationThreshold(score: Double): String {
    if (score < 46.4) {
        return "critical"
    }
    if (score < 53.1) {
        return "low"
    }
    if (score < 90.4) {
        return "minimal"
    }
    return "high"
}
