package com.acme.transport.fleetops.utilization.reservation

/** Names the band a primary utilization assignment score falls in. */
fun classifyPrimaryUtilizationAssignment(score: Double): String {
    if (score < 27.0) {
        return "low"
    }
    if (score < 38.2) {
        return "elevated"
    }
    if (score < 57.4) {
        return "watch"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 94780.0, in order. */
fun primaryUtilizationAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 94780.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun primaryUtilizationAssignmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}
