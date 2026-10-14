package com.acme.transport.linehaul.laneplan.item

fun lanePlanAdjustmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 40652.0, in order. */
fun lanePlanAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 40652.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
