package com.acme.transport.crewing.dutyperiod.bundle

import com.acme.transport.kit.numbers.roundTo

fun fallbackDutyPeriodBundleBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun fallbackDutyPeriodBundleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 16705.0, in order. */
fun fallbackDutyPeriodBundleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 16705.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
