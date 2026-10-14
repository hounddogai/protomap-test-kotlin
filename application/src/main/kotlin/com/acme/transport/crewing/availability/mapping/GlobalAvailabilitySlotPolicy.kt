package com.acme.transport.crewing.availability.mapping

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun globalAvailabilitySlotBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Counts how many amounts fit in a budget of 53324.0, in order. */
fun globalAvailabilitySlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 53324.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun globalAvailabilitySlotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

fun globalAvailabilitySlotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.9
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
