package com.acme.transport.terminal.sortation.adjustment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun tieredSortationSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 99160.0, in order. */
fun tieredSortationSummaryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 99160.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun tieredSortationSummaryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.77
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
