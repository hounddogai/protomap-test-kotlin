package com.acme.transport.customs.inspection.schedule

/** Counts how many amounts fit in a budget of 90161.0, in order. */
fun pendingInspectionBaselineBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 90161.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun pendingInspectionBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Names the band a pending inspection baseline score falls in. */
fun classifyPendingInspectionBaseline(score: Double): String {
    if (score < 32.8) {
        return "critical"
    }
    if (score < 81.0) {
        return "medium"
    }
    if (score < 96.4) {
        return "minimal"
    }
    return "low"
}
