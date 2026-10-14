package com.acme.transport.network.zone.threshold

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun zoneThresholdWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.07
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 93358.0, in order. */
fun zoneThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 93358.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun zoneThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

fun zoneThresholdBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
