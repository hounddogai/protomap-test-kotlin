package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

/** Counts how many amounts fit in a budget of 69631.0, in order. */
fun legacyBackhaulBaselineBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 69631.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun legacyBackhaulBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}
