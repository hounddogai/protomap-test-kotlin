package com.acme.transport.fleetops.tire.manifest

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun tireManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 10941.0, in order. */
fun tireManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 10941.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun tireManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.88
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
