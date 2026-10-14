package com.acme.transport.emissions.carbonreport.threshold

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun adaptiveCarbonReportGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.23
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 53077.0, in order. */
fun adaptiveCarbonReportGroupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 53077.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
