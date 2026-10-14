package com.acme.transport.customs.inspection.window

/** Counts how many amounts fit in a budget of 11600.0, in order. */
fun adaptiveInspectionWindowBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 11600.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun adaptiveInspectionWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}
