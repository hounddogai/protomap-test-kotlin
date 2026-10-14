package com.acme.transport.linehaul.backhaul.target

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun adaptiveBackhaulTargetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.24
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun adaptiveBackhaulTargetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Counts how many amounts fit in a budget of 19661.0, in order. */
fun adaptiveBackhaulTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 19661.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun adaptiveBackhaulTargetBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
