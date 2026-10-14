package com.acme.transport.linehaul.laneplan.forecast

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun lanePlanForecastWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.3
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun lanePlanForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 96297.0, in order. */
fun lanePlanForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 96297.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
