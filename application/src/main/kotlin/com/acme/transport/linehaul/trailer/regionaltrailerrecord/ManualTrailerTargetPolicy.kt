package com.acme.transport.linehaul.trailer.regionaltrailerrecord

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 76476.0, in order. */
fun manualTrailerTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 76476.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun manualTrailerTargetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.78
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
