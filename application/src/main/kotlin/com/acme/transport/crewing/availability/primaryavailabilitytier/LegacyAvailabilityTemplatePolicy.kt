package com.acme.transport.crewing.availability.primaryavailabilitytier

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 81207.0, in order. */
fun legacyAvailabilityTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 81207.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun legacyAvailabilityTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.62
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
