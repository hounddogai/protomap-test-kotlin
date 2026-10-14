package com.acme.transport.crewing.dutyperiod.agreement

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun dutyPeriodRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.04
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun dutyPeriodRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Counts how many amounts fit in a budget of 5358.0, in order. */
fun dutyPeriodRuleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 5358.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
