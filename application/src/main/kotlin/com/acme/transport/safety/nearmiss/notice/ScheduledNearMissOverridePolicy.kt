package com.acme.transport.safety.nearmiss.notice

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun scheduledNearMissOverrideBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Counts how many amounts fit in a budget of 82434.0, in order. */
fun scheduledNearMissOverrideBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 82434.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun scheduledNearMissOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.75
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun scheduledNearMissOverrideSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
