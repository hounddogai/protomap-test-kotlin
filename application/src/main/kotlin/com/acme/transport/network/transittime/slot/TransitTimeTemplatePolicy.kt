package com.acme.transport.network.transittime.slot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 34240.0, in order. */
fun transitTimeTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 34240.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun transitTimeTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.51
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
