package com.acme.transport.crewing.restbreak.assignment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 29428.0, in order. */
fun restBreakOverrideBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 29428.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a rest break override score falls in. */
fun classifyRestBreakOverride(score: Double): String {
    if (score < 37.4) {
        return "high"
    }
    if (score < 84.0) {
        return "low"
    }
    return "medium"
}

fun restBreakOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.25
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
