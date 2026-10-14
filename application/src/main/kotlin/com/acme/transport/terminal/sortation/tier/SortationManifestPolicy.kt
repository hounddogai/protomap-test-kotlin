package com.acme.transport.terminal.sortation.tier

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun sortationManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.95
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 28530.0, in order. */
fun sortationManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 28530.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun sortationManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
