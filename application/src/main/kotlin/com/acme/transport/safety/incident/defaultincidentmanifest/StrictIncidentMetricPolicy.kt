package com.acme.transport.safety.incident.defaultincidentmanifest

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun strictIncidentMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.19
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 14092.0, in order. */
fun strictIncidentMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 14092.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun strictIncidentMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}
